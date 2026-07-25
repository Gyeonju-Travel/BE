package com.example.gyeonjutravel.domain.place.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DeleteBookmarkRequest(
        @NotNull(message = "장소 ID는 필수입니다.")
        @Positive(message = "장소 ID는 1 이상이어야 합니다.")
        Long placeId
) {
}
