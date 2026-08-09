package com.example.gyeonjutravel.domain.home.dto.request;

import com.example.gyeonjutravel.domain.home.enums.DogCondition;
import com.example.gyeonjutravel.domain.schedule.entity.DepartureArea;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RecommendedRouteRequest(
        @NotNull(message = "출발지는 필수입니다.")
        DepartureArea departureArea,

        @NotNull(message = "방문 날짜는 필수입니다.")
        @FutureOrPresent(message = "방문 날짜는 오늘 이후여야 합니다.")
        LocalDate date,

        @NotNull(message = "오늘 컨디션은 필수입니다.")
        DogCondition condition
) {
}
