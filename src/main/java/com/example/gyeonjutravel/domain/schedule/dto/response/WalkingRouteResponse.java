package com.example.gyeonjutravel.domain.schedule.dto.response;

import com.example.gyeonjutravel.global.tmap.WalkingRoute;

public record WalkingRouteResponse(
        String fromNodeKey,
        String toNodeKey,
        Long durationSeconds,
        Long distanceMeters
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
