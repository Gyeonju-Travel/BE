package com.example.gyeonjutravel.domain.notification.dto.response;

import java.util.List;

public record NotificationListResponse(
        long unreadCount,
        List<NotificationListItemResponse> notifications
) {
}
