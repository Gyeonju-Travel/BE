package com.example.gyeonjutravel.domain.notification.dto.response;

import com.example.gyeonjutravel.domain.notification.entity.Notification;

import java.time.LocalDateTime;

public record NotificationListItemResponse(
        Long notificationId,
        boolean read,
        LocalDateTime readAt
) {
    public static NotificationListItemResponse from(Notification notification) {
        return new NotificationListItemResponse(
                notification.getId(),
                notification.isRead(),
                notification.getReadAt()
        );
    }
}
