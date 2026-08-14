package com.example.gyeonjutravel.domain.stamp.dto.response;

public record MyPageStampItemResponse(
        String stampName,
        int count
) {
    public static MyPageStampItemResponse of(String stampName) {
        return new MyPageStampItemResponse(stampName, 1);
    }
}
