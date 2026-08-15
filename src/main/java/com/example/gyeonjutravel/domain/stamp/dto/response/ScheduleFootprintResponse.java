package com.example.gyeonjutravel.domain.stamp.dto.response;

import com.example.gyeonjutravel.domain.stamp.entity.StampAlbum;

public record ScheduleFootprintResponse(
        Long scheduleId,
        long totalDistanceMeters,
        int footprintCount
) {
    public static ScheduleFootprintResponse from(StampAlbum album) {
        return new ScheduleFootprintResponse(
                album.getSchedule().getId(),
                album.getTotalDistanceMeters(),
                album.getFootprintCount()
        );
    }
}
