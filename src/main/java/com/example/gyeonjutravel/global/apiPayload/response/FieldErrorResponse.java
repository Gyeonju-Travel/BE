package com.example.gyeonjutravel.global.apiPayload.response;

public record FieldErrorResponse(
        String field,
        String value,
        String reason
) {
}
