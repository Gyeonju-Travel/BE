package com.example.gyeonjutravel.domain.terms.exception;

import com.example.gyeonjutravel.global.apiPayload.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum TermsErrorCode implements BaseErrorCode {
    TERMS_AGREEMENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "TERMS_400_1", "약관 동의 정보가 유효하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    TermsErrorCode(HttpStatus status, String code, String message) {
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
