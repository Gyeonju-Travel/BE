package com.example.gyeonjutravel.domain.stamp.dto.response;

import com.example.gyeonjutravel.domain.stamp.entity.PlaceVisit;
import com.example.gyeonjutravel.domain.stamp.entity.StampType;

import java.time.LocalDateTime;

public record PlaceVisitResponse(
        Long visitId,
        Long scheduleId,
        Long placeId,
        String placeName,
        long distanceMeters,
        String stampName,
        LocalDateTime visitedAt
) {
    public static PlaceVisitResponse of(PlaceVisit visit, long distanceMeters, StampType stampType) {
        return new PlaceVisitResponse(
                visit.getId(),
                visit.getSchedule().getId(),
                visit.getPlace().getId(),
                visit.getPlace().getName(),
                distanceMeters,
                stampType.getDisplayName(),
                visit.getVisitedAt()
        );
    }
}
