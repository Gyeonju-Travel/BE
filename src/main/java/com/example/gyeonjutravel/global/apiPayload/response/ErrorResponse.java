package com.example.gyeonjutravel.global.apiPayload.response;

import java.util.List;

import com.example.gyeonjutravel.global.apiPayload.exception.BaseErrorCode;

public record ErrorResponse(
        Boolean isSuccess,
        String code,
        String message,
        Object result
) {

    public static ErrorResponse of(BaseErrorCode errorCode) {
        return of(errorCode, errorCode.getMessage());
    }

    public static ErrorResponse of(BaseErrorCode errorCode, String message) {
        return new ErrorResponse(
                false,
                errorCode.getCode(),
                message,
                null
        );
    }

    public static ErrorResponse of(BaseErrorCode errorCode, List<FieldErrorResponse> errors) {
        return new ErrorResponse(
                false,
                errorCode.getCode(),
                errorCode.getMessage(),
                errors
        );
    }
}
