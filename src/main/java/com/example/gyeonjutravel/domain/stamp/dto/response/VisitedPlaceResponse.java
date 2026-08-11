package com.example.gyeonjutravel.domain.stamp.dto.response;

import com.example.gyeonjutravel.domain.stamp.entity.PlaceVisit;

import java.time.LocalDateTime;

public record VisitedPlaceResponse(
        Long placeId,
        String placeName,
        String imageUrl,
        LocalDateTime visitedAt
) {
    public static VisitedPlaceResponse from(PlaceVisit visit) {
        return new VisitedPlaceResponse(
                visit.getPlace().getId(),
                visit.getPlace().getName(),
                visit.getPlace().getImageUrl(),
                visit.getVisitedAt()
        );
    }
}
