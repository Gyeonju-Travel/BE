package com.example.gyeonjutravel.domain.home.dto.response;

import com.example.gyeonjutravel.domain.home.enums.RecommendedRouteStatus;
import com.example.gyeonjutravel.domain.schedule.dto.response.SchedulePreviewResponse;

public record RecommendedRouteStatusResponse(
        String recommendationId,
        RecommendedRouteStatus status,
        SchedulePreviewResponse result,
        String errorMessage
) {
}
