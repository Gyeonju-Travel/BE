package com.example.gyeonjutravel.domain.notification.dto.response;

import com.example.gyeonjutravel.domain.notification.entity.Notification;
import com.example.gyeonjutravel.domain.notification.entity.NotificationType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record NotificationResponse(
        Long notificationId,
        NotificationType type,
        Long scheduleId,
        LocalDate travelDate,
        String title,
        String body,
        String targetUrl,
        LocalDateTime scheduledAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getSchedule().getId(),
                notification.getSchedule().getTravelDate(),
                notification.getTitle(),
                notification.getBody(),
                notification.getTargetUrl(),
                notification.getScheduledAt()
        );
    }
}
