package com.example.gyeonjutravel.domain.report.dto.response;

import com.example.gyeonjutravel.domain.report.entity.PlaceReport;
import com.example.gyeonjutravel.domain.report.entity.enums.PlaceReportStatus;

import java.time.LocalDate;

public record PlaceReportCreateResponse(
        Long placeReportId,
        PlaceReportStatus status,
        String imageUrl,
        LocalDate submittedAt
) {
    public static PlaceReportCreateResponse from(PlaceReport report, java.util.function.Function<String, String> imageUrl) {
        return new PlaceReportCreateResponse(
                report.getId(),
                report.getStatus(),
                imageUrl.apply(report.getImageUrl()),
                report.getCreatedAt().toLocalDate()
        );
    }
}
