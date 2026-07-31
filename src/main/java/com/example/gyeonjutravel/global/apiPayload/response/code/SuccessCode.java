package com.example.gyeonjutravel.global.apiPayload.response.code;

import org.springframework.http.HttpStatus;

public enum SuccessCode {

    OK(HttpStatus.OK, "COMMON_200", "요청에 성공했습니다."),
    CREATED(HttpStatus.CREATED, "COMMON_201", "리소스가 생성되었습니다."),
    DELETED(HttpStatus.OK, "COMMON_200", "리소스가 삭제되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    SuccessCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
