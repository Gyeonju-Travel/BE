package com.example.gyeonjutravel.domain.pet.exception;

import com.example.gyeonjutravel.global.apiPayload.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum PetErrorCode implements BaseErrorCode {

    PET_NOT_FOUND(HttpStatus.NOT_FOUND, "PET_404_1", "반려견을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    PetErrorCode(HttpStatus status, String code, String message) {
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
