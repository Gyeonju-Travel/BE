package com.example.gyeonjutravel.domain.stamp.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FootprintAddRequest(
        @NotNull
        @Positive
        Long distanceMeters
) {
}
