package com.example.gyeonjutravel.domain.schedule.dto.response;

import com.example.gyeonjutravel.domain.schedule.entity.Schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ScheduleStartResponse(
        Long scheduleId,
        LocalDate date,
        boolean started,
        LocalDateTime startedAt,
        DepartureResponse departure,
        List<SchedulePlaceDetailResponse> places
) {
    public static ScheduleStartResponse from(Schedule schedule) {
        return new ScheduleStartResponse(
                schedule.getId(),
                schedule.getTravelDate(),
                schedule.isStarted(),
                schedule.getStartedAt(),
                DepartureResponse.from(schedule.getDepartureArea()),
                schedule.getItems().stream()
                        .map(SchedulePlaceDetailResponse::from)
                        .toList()
        );
    }
}
