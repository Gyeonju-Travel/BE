package com.example.gyeonjutravel.domain.schedule.dto.response;

import com.example.gyeonjutravel.domain.schedule.entity.Schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record ScheduleDetailResponse(
        Long scheduleId,
        LocalDate date,
        DepartureResponse departure,
        String lastPlaceName,
        List<String> placeNames,
        int totalPlaceCount,
        long totalWalkingDurationSeconds,
        boolean started,
        LocalDateTime startedAt
) {
    public static ScheduleDetailResponse from(Schedule schedule) {
        List<SchedulePlaceDetailResponse> places = schedule.getItems().stream()
                .map(SchedulePlaceDetailResponse::from)
                .toList();
        long totalWalkingDurationSeconds = schedule.getItems().stream()
                .mapToLong(item -> item.getWalkingDurationSeconds())
                .sum();
        String lastPlaceName = schedule.getItems().stream()
                .max(Comparator.comparingInt(item -> item.getVisitOrder()))
                .map(item -> item.getPlace().getName())
                .orElse(null);
        return new ScheduleDetailResponse(
                schedule.getId(),
                schedule.getTravelDate(),
                DepartureResponse.from(schedule.getDepartureArea()),
                lastPlaceName,
                places.stream()
                        .map(SchedulePlaceDetailResponse::name)
                        .toList(),
                places.size(),
                totalWalkingDurationSeconds,
                schedule.isStarted(),
                schedule.getStartedAt()
        );
    }
}
