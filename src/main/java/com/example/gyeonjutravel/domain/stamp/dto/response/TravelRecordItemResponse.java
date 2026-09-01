package com.example.gyeonjutravel.domain.stamp.dto.response;

import com.example.gyeonjutravel.domain.schedule.entity.Schedule;
import com.example.gyeonjutravel.domain.schedule.entity.ScheduleItem;
import com.example.gyeonjutravel.domain.stamp.entity.StampAlbum;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public record TravelRecordItemResponse(
        Long scheduleId,
        LocalDate date,
        String title,
        String photoUrl,
        int totalPlaceCount,
        long totalWalkingDurationSeconds
) {
    public static TravelRecordItemResponse from(Schedule schedule, StampAlbum album) {
        List<ScheduleItem> orderedItems = schedule.getItems().stream()
                .sorted(Comparator.comparingInt(ScheduleItem::getVisitOrder))
                .toList();
        long totalWalkingDurationSeconds = orderedItems.stream()
                .map(ScheduleItem::getWalkingDurationSeconds)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();
        return new TravelRecordItemResponse(
                schedule.getId(),
                schedule.getTravelDate(),
                titleOf(orderedItems),
                photoUrlOf(album),
                orderedItems.size(),
                totalWalkingDurationSeconds
        );
    }

    private static String titleOf(List<ScheduleItem> items) {
        if (items.isEmpty()) {
            return null;
        }
        String firstPlaceName = items.get(0).getPlace().getName();
        String lastPlaceName = items.get(items.size() - 1).getPlace().getName();
        if (firstPlaceName.equals(lastPlaceName)) {
            return firstPlaceName;
        }
        return firstPlaceName + " -> " + lastPlaceName;
    }

    private static String photoUrlOf(StampAlbum album) {
        if (album == null || album.getPhotos().isEmpty()) {
            return null;
        }
        return album.getPhotos().get(0).getImageUrl();
    }
}
