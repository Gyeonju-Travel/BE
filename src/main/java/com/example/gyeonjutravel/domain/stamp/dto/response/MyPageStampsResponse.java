package com.example.gyeonjutravel.domain.stamp.dto.response;

import java.util.List;

public record MyPageStampsResponse(
        int totalStampCount,
        List<MyPageStampItemResponse> stamps
) {
    public static MyPageStampsResponse from(List<MyPageStampItemResponse> stamps) {
        return new MyPageStampsResponse(stamps.size(), stamps);
    }
}
