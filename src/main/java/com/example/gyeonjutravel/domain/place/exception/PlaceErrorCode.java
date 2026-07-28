package com.example.gyeonjutravel.domain.place.exception;

import com.example.gyeonjutravel.global.apiPayload.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum PlaceErrorCode implements BaseErrorCode {
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE_404_1", "장소를 찾을 수 없습니다."),
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE_404_2", "저장한 장소를 찾을 수 없습니다."),
    BOOKMARK_ALREADY_EXISTS(HttpStatus.CONFLICT, "PLACE_409_1", "이미 저장한 장소입니다."),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "PLACE_400_1", "페이지 크기는 1 이상 200 이하여야 합니다."),
    INVALID_BOOKMARK_PLACE_IDS(HttpStatus.BAD_REQUEST, "PLACE_400_2", "삭제할 장소 ID를 한 개 이상 입력해야 합니다.");

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
