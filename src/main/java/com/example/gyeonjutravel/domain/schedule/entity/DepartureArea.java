package com.example.gyeonjutravel.domain.schedule.entity;

import lombok.Getter;

@Getter
public enum DepartureArea {
    HWANGRIDAN_GIL("황리단길", 129.20995370588062, 35.83740829748873),
    GEUMRIDAN_GIL("금리단길", 129.213503362291, 35.8423816699688),
    CHEOMSEONGDAE("첨성대", 129.2185644826, 35.8343745291),
    GYOCHON_VILLAGE("교촌마을", 129.214693367401, 35.8296308266303);

    private final String label;
    private final double longitude;
    private final double latitude;

    DepartureArea(String label, double longitude, double latitude) {
        this.label = label;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
