package com.example.gyeonjutravel.domain.notification.dto.request;

import jakarta.validation.constraints.NotNull;

public record NotificationSettingUpdateRequest(
        @NotNull Boolean enabled
) {
}
