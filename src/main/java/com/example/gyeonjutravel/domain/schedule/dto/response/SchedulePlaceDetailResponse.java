package com.example.gyeonjutravel.domain.schedule.dto.response;

import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.schedule.entity.ScheduleItem;

public record SchedulePlaceDetailResponse(
        int visitOrder,
        Long placeId,
        String name,
        String category,
        String categoryLabel,
        String imageUrl,
        String petAccessType,
        String petRequirements,
        Long walkingDurationSeconds,
        Long walkingDistanceMeters,
        double longitude,
        double latitude
) {
    public static SchedulePlaceDetailResponse from(ScheduleItem item) {
        Place place = item.getPlace();
        return new SchedulePlaceDetailResponse(
                item.getVisitOrder(),
                place.getId(),
                place.getName(),
                place.getCategory().name(),
                place.getCategory().getLabel(),
                place.getImageUrl(),
                place.getPetAccessType(),
                place.getPetRequirements(),
                item.getWalkingDurationSeconds(),
                item.getWalkingDistanceMeters(),
                place.getLongitude(),
                place.getLatitude()
        );
    }
}
