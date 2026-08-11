package com.example.gyeonjutravel.domain.stamp.dto.response;

import com.example.gyeonjutravel.domain.stamp.entity.PlaceVisit;

import java.time.LocalDateTime;

public record PlaceVisitResponse(
        Long visitId,
        Long scheduleId,
        Long placeId,
        String placeName,
        long distanceMeters,
        int stampCount,
        LocalDateTime visitedAt
) {
    public static PlaceVisitResponse of(PlaceVisit visit, long distanceMeters, int stampCount) {
        return new PlaceVisitResponse(
                visit.getId(),
                visit.getSchedule().getId(),
                visit.getPlace().getId(),
                visit.getPlace().getName(),
                distanceMeters,
                stampCount,
                visit.getVisitedAt()
        );
    }
}
