package com.example.gyeonjutravel.domain.home.dto.response;

import com.example.gyeonjutravel.domain.schedule.dto.response.DepartureResponse;

import java.time.LocalDate;
import java.util.List;

public record RecommendedRouteResultResponse(
        Long recommendationId,
        LocalDate date,
        DepartureResponse departure,
        List<RecommendedRoutePlaceResponse> recommendedPlaces
) {
}
