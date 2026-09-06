package com.example.gyeonjutravel.domain.place.service;

import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.place.entity.PlaceCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MapOnlyPlacesTest {

    @Test
    void identifiesOnlyTheSevenAdditionalCafes() {
        assertThat(MapOnlyPlaces.isMapOnly(place("로라커피"))).isTrue();
        assertThat(MapOnlyPlaces.isMapOnly(place("yesterday"))).isTrue();
        assertThat(MapOnlyPlaces.isMapOnly(place("시간의 여백"))).isFalse();
    }

    private Place place(String name) {
        return Place.builder()
                .sourceKey("TEST:" + name)
                .category(PlaceCategory.CAFE)
                .originalCategory("카페")
                .name(name)
                .roadAddress("경북 경주시")
                .longitude(129.21)
                .latitude(35.84)
                .build();
    }
}
