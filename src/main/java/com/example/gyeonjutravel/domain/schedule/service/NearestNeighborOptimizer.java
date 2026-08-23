package com.example.gyeonjutravel.domain.schedule.service;

import com.example.gyeonjutravel.domain.schedule.exception.ScheduleErrorCode;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.example.gyeonjutravel.global.tmap.WalkingMatrix;
import com.example.gyeonjutravel.global.tmap.WalkingRoute;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class NearestNeighborOptimizer {

    public List<Long> optimize(List<Long> placeIds, WalkingMatrix matrix) {
        Set<Long> unvisited = new LinkedHashSet<>(placeIds);
        List<Long> orderedPlaceIds = new ArrayList<>(placeIds.size());
        String currentNodeKey = ScheduleMatrixCache.START_NODE_KEY;

        while (!unvisited.isEmpty()) {
            String fromNodeKey = currentNodeKey;
            Long nextPlaceId = unvisited.stream()
                    .min(Comparator
                            .comparingLong((Long placeId) -> route(
                                    matrix,
                                    fromNodeKey,
                                    ScheduleMatrixCache.placeNodeKey(placeId)
                            ).durationSeconds())
                            .thenComparingLong(Long::longValue))
                    .orElseThrow(() -> new GeneralException(ScheduleErrorCode.WALKING_ROUTE_NOT_FOUND));

            orderedPlaceIds.add(nextPlaceId);
            unvisited.remove(nextPlaceId);
            currentNodeKey = ScheduleMatrixCache.placeNodeKey(nextPlaceId);
        }
        return orderedPlaceIds;
    }

    public List<Long> optimizeAvoidingConsecutive(
            List<Long> placeIds,
            Set<Long> separatedPlaceIds,
            WalkingMatrix matrix
    ) {
        List<List<Long>> candidates = new ArrayList<>();
        collectValidOrders(
                placeIds,
                separatedPlaceIds,
                new ArrayList<>(placeIds.size()),
                new LinkedHashSet<>(placeIds),
                candidates
        );
        return candidates.stream()
                .min(Comparator.comparingLong(order -> totalDurationSeconds(order, matrix)))
                .orElseThrow(() -> new GeneralException(ScheduleErrorCode.WALKING_ROUTE_NOT_FOUND));
    }

    private void collectValidOrders(
            List<Long> placeIds,
            Set<Long> separatedPlaceIds,
            List<Long> current,
            Set<Long> unvisited,
            List<List<Long>> candidates
    ) {
        if (unvisited.isEmpty()) {
            candidates.add(List.copyOf(current));
            return;
        }
        for (Long placeId : placeIds) {
            if (!unvisited.contains(placeId) || isConsecutiveSeparatedPlace(current, placeId, separatedPlaceIds)) {
                continue;
            }
            current.add(placeId);
            unvisited.remove(placeId);
            collectValidOrders(placeIds, separatedPlaceIds, current, unvisited, candidates);
            unvisited.add(placeId);
            current.remove(current.size() - 1);
        }
    }

    private boolean isConsecutiveSeparatedPlace(
            List<Long> current,
            Long nextPlaceId,
            Set<Long> separatedPlaceIds
    ) {
        return !current.isEmpty()
                && separatedPlaceIds.contains(current.get(current.size() - 1))
                && separatedPlaceIds.contains(nextPlaceId);
    }

    private long totalDurationSeconds(List<Long> placeIds, WalkingMatrix matrix) {
        long totalDurationSeconds = 0;
        String previousNodeKey = ScheduleMatrixCache.START_NODE_KEY;
        for (Long placeId : placeIds) {
            String placeNodeKey = ScheduleMatrixCache.placeNodeKey(placeId);
            totalDurationSeconds += route(matrix, previousNodeKey, placeNodeKey).durationSeconds();
            previousNodeKey = placeNodeKey;
        }
        return totalDurationSeconds;
    }

    private WalkingRoute route(WalkingMatrix matrix, String fromNodeKey, String toNodeKey) {
        return matrix.findRoute(fromNodeKey, toNodeKey)
                .orElseThrow(() -> new GeneralException(ScheduleErrorCode.WALKING_ROUTE_NOT_FOUND));
    }
}
