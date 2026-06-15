package com.example.gyeonjutravel.global.apiPayload.response;

import java.util.List;

public record ErrorResponse(
        Boolean isSuccess,
        String code,
        String message,
        Object result
) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return of(errorCode, errorCode.getMessage());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(
                false,
                errorCode.getCode(),
                message,
                null
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldErrorResponse> errors) {
        return new ErrorResponse(
                false,
                errorCode.getCode(),
                errorCode.getMessage(),
                errors
        );
    }
}
