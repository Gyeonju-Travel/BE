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

    private WalkingRoute route(WalkingMatrix matrix, String fromNodeKey, String toNodeKey) {
        return matrix.findRoute(fromNodeKey, toNodeKey)
                .orElseThrow(() -> new GeneralException(ScheduleErrorCode.WALKING_ROUTE_NOT_FOUND));
    }
}
