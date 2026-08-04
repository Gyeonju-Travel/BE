package com.example.gyeonjutravel.domain.schedule.dto.request;

import com.example.gyeonjutravel.domain.schedule.entity.DepartureArea;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ScheduleUpdateRequest(
        @NotBlank String matrixToken,
        @NotNull DepartureArea departureArea,
        @NotEmpty @Size(max = 5) List<@NotNull @Positive Long> orderedPlaceIds
) {
}
