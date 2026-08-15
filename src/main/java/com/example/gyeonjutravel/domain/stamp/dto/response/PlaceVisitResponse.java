package com.example.gyeonjutravel.domain.stamp.dto.response;

import com.example.gyeonjutravel.domain.stamp.entity.PlaceVisit;
import com.example.gyeonjutravel.domain.stamp.entity.StampType;

import java.time.LocalDateTime;

public record PlaceVisitResponse(
        Long visitId,
        Long scheduleId,
        String stampName,
        LocalDateTime visitedAt
) {
    public static PlaceVisitResponse of(PlaceVisit visit, StampType stampType) {
        return new PlaceVisitResponse(
                visit.getId(),
                visit.getSchedule().getId(),
                stampType.getDisplayName(),
                visit.getVisitedAt()
        );
    }
}
