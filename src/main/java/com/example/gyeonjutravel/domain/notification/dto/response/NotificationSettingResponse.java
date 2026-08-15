package com.example.gyeonjutravel.domain.notification.dto.response;

import com.example.gyeonjutravel.domain.notification.entity.NotificationSetting;

public record NotificationSettingResponse(boolean enabled) {

    public static NotificationSettingResponse from(NotificationSetting setting) {
        return new NotificationSettingResponse(setting.isEnabled());
    }
}
