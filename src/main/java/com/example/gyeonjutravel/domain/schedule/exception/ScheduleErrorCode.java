package com.example.gyeonjutravel.domain.schedule.exception;

import com.example.gyeonjutravel.global.apiPayload.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum ScheduleErrorCode implements BaseErrorCode {
    INVALID_PLACE_SELECTION(HttpStatus.BAD_REQUEST, "SCHEDULE_400_1", "일정에 넣을 저장 장소를 올바르게 선택해주세요."),
    PLACE_NOT_BOOKMARKED(HttpStatus.BAD_REQUEST, "SCHEDULE_400_2", "선택한 장소 중 저장하지 않은 장소가 있습니다."),
    INVALID_PLACE_ORDER(HttpStatus.BAD_REQUEST, "SCHEDULE_400_3", "미리보기와 동일한 장소를 중복 없이 정렬해주세요."),
    PREVIEW_EXPIRED(HttpStatus.GONE, "SCHEDULE_410_1", "일정 미리보기가 만료되었습니다. 다시 계산해주세요."),
    WALKING_ROUTE_NOT_FOUND(HttpStatus.UNPROCESSABLE_ENTITY, "SCHEDULE_422_1", "선택한 장소 사이의 도보 경로를 찾을 수 없습니다."),
    TMAP_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "SCHEDULE_503_1", "TMAP API 키가 설정되지 않았습니다."),
    TMAP_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "SCHEDULE_502_1", "TMAP 도보 경로 계산에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ScheduleErrorCode(HttpStatus status, String code, String message) {
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
