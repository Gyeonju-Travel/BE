package com.example.gyeonjutravel.domain.schedule.dto.response;

import com.example.gyeonjutravel.domain.schedule.entity.DepartureArea;

public record DepartureResponse(
        String code,
        String name,
        double longitude,
        double latitude
) {
    public static DepartureResponse from(DepartureArea departureArea) {
        return new DepartureResponse(
                departureArea.name(),
                departureArea.getLabel(),
                departureArea.getLongitude(),
                departureArea.getLatitude()
        );
    }
}
