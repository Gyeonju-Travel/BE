package com.example.gyeonjutravel.global.tmap;

public record WalkingRoute(
        String fromNodeKey,
        String toNodeKey,
        Long durationSeconds,
        Long distanceMeters
) {
}
