package com.example.gyeonjutravel.domain.schedule.service;

import com.example.gyeonjutravel.global.tmap.WalkingMatrix;
import com.example.gyeonjutravel.global.tmap.WalkingRoute;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class NearestNeighborOptimizerTest {

    private final NearestNeighborOptimizer optimizer = new NearestNeighborOptimizer();

    @Test
    void separatesCafesUsingTheShortestValidWalkingOrder() {
        List<Long> placeIds = List.of(1L, 2L, 3L, 4L, 5L);
        WalkingMatrix matrix = matrix(placeIds, List.of(1L, 2L, 3L, 4L, 5L));

        assertThat(optimizer.optimize(placeIds, matrix))
                .containsExactly(1L, 2L, 3L, 4L, 5L);

        List<Long> result = optimizer.optimizeAvoidingConsecutive(
                placeIds,
                Set.of(2L, 3L),
                matrix
        );

        assertThat(result).containsExactly(1L, 2L, 4L, 5L, 3L);
    }

    private WalkingMatrix matrix(List<Long> placeIds, List<Long> shortestOrder) {
        List<String> nodeKeys = new ArrayList<>();
        nodeKeys.add(ScheduleMatrixCache.START_NODE_KEY);
        placeIds.stream().map(ScheduleMatrixCache::placeNodeKey).forEach(nodeKeys::add);

        List<String> preferredNodes = new ArrayList<>();
        preferredNodes.add(ScheduleMatrixCache.START_NODE_KEY);
        shortestOrder.stream().map(ScheduleMatrixCache::placeNodeKey).forEach(preferredNodes::add);

        List<WalkingRoute> routes = new ArrayList<>();
        for (String from : nodeKeys) {
            for (String to : nodeKeys) {
                if (from.equals(to)) {
                    continue;
                }
                long duration = isPreferredEdge(from, to, preferredNodes) ? 1 : 100;
                routes.add(new WalkingRoute(from, to, duration, duration));
            }
        }
        return new WalkingMatrix(nodeKeys, routes);
    }

    private boolean isPreferredEdge(String from, String to, List<String> preferredNodes) {
        int fromIndex = preferredNodes.indexOf(from);
        return fromIndex >= 0
                && fromIndex + 1 < preferredNodes.size()
                && preferredNodes.get(fromIndex + 1).equals(to);
    }
}
