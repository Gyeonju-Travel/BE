package com.example.gyeonjutravel.domain.pet.exception;

import com.example.gyeonjutravel.global.apiPayload.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum PetErrorCode implements BaseErrorCode {

    PET_NOT_FOUND(HttpStatus.NOT_FOUND, "PET_404_1", "반려견을 찾을 수 없습니다."),
    PET_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "PET_404_2", "반려견 이미지를 찾을 수 없습니다."),
    INVALID_PET_IMAGE(HttpStatus.BAD_REQUEST, "PET_400_1", "JPEG, PNG, WEBP 형식의 이미지만 등록할 수 있습니다."),
    ONBOARDING_ALREADY_COMPLETED(HttpStatus.CONFLICT, "PET_409_1", "이미 온보딩 반려견이 등록되어 있습니다."),
    PET_IMAGE_STORAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PET_500_1", "반려견 이미지 저장에 실패했습니다.");

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
