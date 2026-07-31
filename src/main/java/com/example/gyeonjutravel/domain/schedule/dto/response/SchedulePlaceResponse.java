package com.example.gyeonjutravel.domain.schedule.dto.response;

import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.place.entity.PlaceCategory;

public record SchedulePlaceResponse(
        int visitOrder,
        Long placeId,
        String name,
        String imageUrl,
        String petAccessType,
        String petRequirements,
        Long walkingDurationSeconds,
        Long walkingDistanceMeters
) {
    public static SchedulePlaceResponse preview(
            int visitOrder,
            Place place,
            long walkingDurationSeconds,
            long walkingDistanceMeters
    ) {
        return new SchedulePlaceResponse(
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

    public static SchedulePlaceResponse saved(
            int visitOrder,
            Place place,
            long walkingDurationSeconds,
            long walkingDistanceMeters
    ) {
        return new SchedulePlaceResponse(
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
