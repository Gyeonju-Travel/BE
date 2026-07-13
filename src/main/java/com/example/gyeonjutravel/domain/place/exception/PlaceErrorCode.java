package com.example.gyeonjutravel.domain.place.exception;

import com.example.gyeonjutravel.global.apiPayload.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum PlaceErrorCode implements BaseErrorCode {
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE_404_1", "장소를 찾을 수 없습니다."),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "PLACE_400_1", "페이지 크기는 1 이상 200 이하여야 합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    PlaceErrorCode(HttpStatus status, String code, String message) {
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
