package com.example.gyeonjutravel.domain.notification.exception;

import com.example.gyeonjutravel.global.apiPayload.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum NotificationErrorCode implements BaseErrorCode {
    NOTIFICATION_DISABLED(HttpStatus.BAD_REQUEST, "NOTIFICATION_400_1", "알림 설정이 꺼져 있습니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_404_1", "알림을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    NotificationErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
