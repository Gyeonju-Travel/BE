package com.example.gyeonjutravel.domain.schedule.dto.response;

import java.time.LocalDate;
import java.util.List;

public record ScheduleDateResponse(
        LocalDate date,
        int totalScheduleCount,
        List<ScheduleDetailResponse> schedules
) {
    public static ScheduleDateResponse of(LocalDate date, List<ScheduleDetailResponse> schedules) {
        return new ScheduleDateResponse(date, schedules.size(), schedules);
    }
}
