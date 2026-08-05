package com.example.gyeonjutravel.global.tmap;

public record WalkingRoute(
        String fromNodeKey,
        String toNodeKey,
        long durationSeconds,
        long distanceMeters
) {
}
