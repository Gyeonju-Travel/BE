package com.example.gyeonjutravel.domain.stamp.exception;

import com.example.gyeonjutravel.global.apiPayload.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum StampErrorCode implements BaseErrorCode {
    REPRESENTATIVE_PET_NOT_FOUND(HttpStatus.BAD_REQUEST, "STAMP_400_1", "대표 반려견을 먼저 등록해 주세요."),
    SCHEDULE_NOT_STARTED(HttpStatus.BAD_REQUEST, "STAMP_400_2", "일정을 시작한 뒤 발자국을 기록할 수 있습니다."),
    LOCATION_TIME_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, "STAMP_400_3", "일정 시작 시각부터 오후 9시까지만 발자국을 기록할 수 있습니다."),
    INVALID_PHOTO_COUNT(HttpStatus.BAD_REQUEST, "STAMP_400_4", "스탬프 앨범 사진은 정확히 2장을 선택해 주세요."),
    PLACE_TOO_FAR(HttpStatus.BAD_REQUEST, "STAMP_400_5", "관광지 40m 이내에서만 스탬프를 받을 수 있습니다."),
    PET_NOT_FOUND(HttpStatus.NOT_FOUND, "STAMP_404_1", "반려견을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    StampErrorCode(HttpStatus status, String code, String message) {
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
