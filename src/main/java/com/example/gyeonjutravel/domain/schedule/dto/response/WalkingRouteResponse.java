package com.example.gyeonjutravel.domain.schedule.dto.response;

import com.example.gyeonjutravel.global.tmap.WalkingRoute;

public record WalkingRouteResponse(
        String fromNodeKey,
        String toNodeKey,
        long durationSeconds,
        long distanceMeters
) {
    public static WalkingRouteResponse from(WalkingRoute route) {
        return new WalkingRouteResponse(
                route.fromNodeKey(),
                route.toNodeKey(),
                route.durationSeconds(),
                route.distanceMeters()
        );
    }
}
