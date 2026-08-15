package com.example.gyeonjutravel.domain.home.dto.response;

import com.example.gyeonjutravel.domain.home.enums.RecommendedRouteStatus;

public record RecommendedRouteJobResponse(
        Long recommendationId,
        RecommendedRouteStatus status
) {
}
