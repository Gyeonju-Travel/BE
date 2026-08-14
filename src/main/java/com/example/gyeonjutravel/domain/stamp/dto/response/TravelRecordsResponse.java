package com.example.gyeonjutravel.domain.stamp.dto.response;

import java.util.List;

public record TravelRecordsResponse(
        int totalTravelCount,
        int totalVisitedPlaceCount,
        int totalStampCount,
        List<TravelRecordItemResponse> records
) {
    public static TravelRecordsResponse of(
            List<TravelRecordItemResponse> records,
            int totalStampCount
    ) {
        int totalVisitedPlaceCount = records.stream()
                .mapToInt(TravelRecordItemResponse::totalPlaceCount)
                .sum();
        return new TravelRecordsResponse(
                records.size(),
                totalVisitedPlaceCount,
                totalStampCount,
                records
        );
    }
}
