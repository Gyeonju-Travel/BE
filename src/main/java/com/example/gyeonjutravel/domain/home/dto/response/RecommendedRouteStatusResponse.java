package com.example.gyeonjutravel.domain.home.dto.response;

import com.example.gyeonjutravel.domain.home.enums.RecommendedRouteStatus;
import com.example.gyeonjutravel.domain.home.enums.RecommendedRouteStep;

public record RecommendedRouteStatusResponse(
        Long recommendationId,
        RecommendedRouteStatus status,
        RecommendedRouteStep step,
        String message,
        String errorMessage
) {
}
