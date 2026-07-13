package com.example.gyeonjutravel.domain.place.entity;

public enum PlaceCategory {
    RESTAURANT("식당"),
    CAFE("카페"),
    ATTRACTION("관광지");

    private final String label;

    PlaceCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
