package com.example.gyeonjutravel.domain.schedule.dto.request;

import com.example.gyeonjutravel.domain.schedule.entity.DepartureArea;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record SchedulePreviewRequest(
        @NotNull(message = "출발지는 필수입니다.")
        DepartureArea departureArea,

        @NotNull(message = "일정 날짜는 필수입니다.")
        @FutureOrPresent(message = "일정 날짜는 오늘 이후여야 합니다.")
        LocalDate date,

        @NotEmpty(message = "장소를 한 개 이상 선택해주세요.")
        @Size(max = 4, message = "장소는 최대 4개까지 선택할 수 있습니다.")
        List<@NotNull @Positive Long> placeIds
) {
}
