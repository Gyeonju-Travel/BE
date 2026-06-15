package com.example.gyeonjutravel.global.apiPayload;

import com.example.gyeonjutravel.global.apiPayload.response.SuccessCode;

public record ApiResponse<T>(
        Boolean isSuccess,
        String code,
        String message,
        T result
) {

    public static <T> ApiResponse<T> ok(T result) {
        return of(SuccessCode.OK, result);
    }

    public static ApiResponse<Void> ok() {
        return of(SuccessCode.OK, null);
    }

    public static <T> ApiResponse<T> created(T result) {
        return of(SuccessCode.CREATED, result);
    }

    public static ApiResponse<Void> created() {
        return of(SuccessCode.CREATED, null);
    }

    public static ApiResponse<Void> deleted() {
        return of(SuccessCode.DELETED, null);
    }

    public static <T> ApiResponse<T> of(SuccessCode successCode, T result) {
        return new ApiResponse<>(
                true,
                successCode.getCode(),
                successCode.getMessage(),
                result
        );
    }
}
