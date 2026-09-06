package com.example.gyeonjutravel.domain.home.dto.response;

import com.example.gyeonjutravel.domain.place.entity.Place;

public record RecommendedRoutePlaceResponse(
        int visitOrder,
        Long placeId,
        String name,
        String category,
        String categoryLabel,
        String imageUrl,
        String petAccessType,
        String petRequirements,
        Long walkingDurationSeconds,
        Long walkingDistanceMeters
) {
    public static RecommendedRoutePlaceResponse of(
            int visitOrder,
            Place place,
            Long walkingDurationSeconds,
            Long walkingDistanceMeters
    ) {
        return new RecommendedRoutePlaceResponse(
                visitOrder,
                place.getId(),
                place.getName(),
                place.getCategory().name(),
                place.getCategory().getLabel(),
                place.getImageUrl(),
                place.getPetAccessType(),
                place.getPetRequirements(),
                walkingDurationSeconds,
                walkingDistanceMeters
        );
    }
}
