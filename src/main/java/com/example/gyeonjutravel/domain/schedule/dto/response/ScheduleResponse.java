package com.example.gyeonjutravel.domain.schedule.dto.response;

import java.time.LocalDate;
import java.util.List;

public record ScheduleResponse(
        Long scheduleId,
        LocalDate date,
        DepartureResponse departure,
        List<SchedulePlaceResponse> places
) {
}
