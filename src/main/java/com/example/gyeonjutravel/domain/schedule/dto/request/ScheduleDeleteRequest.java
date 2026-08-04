package com.example.gyeonjutravel.domain.schedule.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record ScheduleDeleteRequest(
        @NotEmpty(message = "삭제할 일정 ID를 한 개 이상 입력해야 합니다.")
        List<@NotNull @Positive Long> scheduleIds
) {
}
