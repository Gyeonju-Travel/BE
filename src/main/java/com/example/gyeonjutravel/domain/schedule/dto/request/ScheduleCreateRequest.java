package com.example.gyeonjutravel.domain.schedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ScheduleCreateRequest(
        @NotBlank(message = "미리보기 토큰은 필수입니다.")
        String matrixToken,

        @NotEmpty(message = "장소 순서는 필수입니다.")
        @Size(max = 5, message = "장소는 최대 5개까지 저장할 수 있습니다.")
        List<@NotNull @Positive Long> orderedPlaceIds
) {
}
