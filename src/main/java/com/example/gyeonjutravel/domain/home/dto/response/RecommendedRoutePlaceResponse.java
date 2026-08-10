package com.example.gyeonjutravel.domain.home.dto.response;

import com.example.gyeonjutravel.domain.place.entity.Place;

public record RecommendedRoutePlaceResponse(
        int visitOrder,
        Long placeId,
        String name,
        String imageUrl,
        String petAccessType,
        String petRequirements,
        Long walkingDurationSeconds,
        Long walkingDistanceMeters
) {
    public static RecommendedRoutePlaceResponse of(
            int visitOrder,
            Place place,
            long walkingDurationSeconds,
            long walkingDistanceMeters
    ) {
        return new RecommendedRoutePlaceResponse(
                visitOrder,
                place.getId(),
                place.getName(),
                place.getImageUrl(),
                place.getPetAccessType(),
                place.getPetRequirements(),
                walkingDurationSeconds,
                walkingDistanceMeters
        );
    }
}
