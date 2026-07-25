package com.example.gyeonjutravel.domain.place.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record DeleteBookmarksRequest(
        @NotEmpty(message = "삭제할 장소 ID를 한 개 이상 입력해야 합니다.")
        @Valid
        List<@NotNull(message = "장소 ID는 필수입니다.")
                @Positive(message = "장소 ID는 1 이상이어야 합니다.") Long> placeIds
) {
}
