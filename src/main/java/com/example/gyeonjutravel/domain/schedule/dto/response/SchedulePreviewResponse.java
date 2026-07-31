package com.example.gyeonjutravel.domain.schedule.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record SchedulePreviewResponse(
        String matrixToken,
        Instant expiresAt,
        LocalDate date,
        DepartureResponse departure,
        List<SchedulePlaceResponse> recommendedPlaces,
        List<WalkingRouteResponse> walkingTimeMatrix
) {
}
