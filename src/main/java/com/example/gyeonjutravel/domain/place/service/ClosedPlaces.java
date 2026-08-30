package com.example.gyeonjutravel.domain.place.service;

import com.example.gyeonjutravel.domain.place.entity.Place;

import java.util.Set;

public final class ClosedPlaces {

    public static final Set<String> NAMES = Set.of("제이커피 경주첨성대점");

    private ClosedPlaces() {
    }

    public static boolean isOpen(Place place) {
        return !NAMES.contains(place.getName());
    }
}
