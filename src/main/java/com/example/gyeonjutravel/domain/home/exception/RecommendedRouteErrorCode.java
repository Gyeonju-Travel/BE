package com.example.gyeonjutravel.domain.home.exception;

import com.example.gyeonjutravel.global.apiPayload.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum RecommendedRouteErrorCode implements BaseErrorCode {
    JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "RECOMMENDED_ROUTE_404_1", "추천 경로 작업을 찾을 수 없습니다."),
    REPRESENTATIVE_PET_NOT_FOUND(HttpStatus.BAD_REQUEST, "RECOMMENDED_ROUTE_400_1", "대표 반려견을 먼저 등록해주세요."),
    NOT_ENOUGH_PLACES(HttpStatus.UNPROCESSABLE_ENTITY, "RECOMMENDED_ROUTE_422_1", "추천 경로를 만들 수 있는 장소 데이터가 부족합니다."),
    INVALID_AI_RESPONSE(HttpStatus.BAD_GATEWAY, "RECOMMENDED_ROUTE_502_1", "AI 추천 결과 형식이 올바르지 않습니다."),
    RECOMMENDED_ROUTE_TOO_FAR(HttpStatus.BAD_GATEWAY, "RECOMMENDED_ROUTE_502_3", "도보 2시간 이내 추천 경로를 만들 수 없습니다."),
    AI_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "RECOMMENDED_ROUTE_503_1", "AI API 키가 설정되지 않았습니다."),
    AI_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "RECOMMENDED_ROUTE_502_2", "AI 추천 요청에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    RecommendedRouteErrorCode(HttpStatus status, String code, String message) {
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
