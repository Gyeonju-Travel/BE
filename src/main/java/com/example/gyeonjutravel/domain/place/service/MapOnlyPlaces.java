package com.example.gyeonjutravel.domain.place.service;

import com.example.gyeonjutravel.domain.place.entity.Place;

import java.util.Set;

public final class MapOnlyPlaces {

    private static final Set<String> NAMES = Set.of(
            "로라커피",
            "소담루",
            "위주",
            "크루그",
            "나봉상점 경주점",
            "연화",
            "yesterday"
    );

    private MapOnlyPlaces() {
    }

    public static boolean containsName(String name) {
        return NAMES.contains(name);
    }

    public static boolean isMapOnly(Place place) {
        return containsName(place.getName());
    }
}
