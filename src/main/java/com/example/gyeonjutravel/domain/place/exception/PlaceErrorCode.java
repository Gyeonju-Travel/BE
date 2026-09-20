package com.example.gyeonjutravel.domain.place.exception;

import com.example.gyeonjutravel.global.apiPayload.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum PlaceErrorCode implements BaseErrorCode {
    TOUR_API_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "PLACE_503_1", "관광공사 API 인증키가 설정되지 않았습니다."),
    TOUR_API_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "PLACE_503_2", "관광공사 관광정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요."),
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
