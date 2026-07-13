package com.example.gyeonjutravel.domain.place.dto.response;

import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.place.entity.PlaceCategory;

public record MapPlaceResponse(
        Long id,
        String name,
        PlaceCategory category,
        String categoryLabel,
        String detailCategory,
        String roadAddress,
        Double longitude,
        Double latitude,
        String imageUrl,
        String petAccessType,
        String petRequirements
) {
    public static MapPlaceResponse from(Place place) {
        return new MapPlaceResponse(
                place.getId(),
                place.getName(),
                place.getCategory(),
                place.getCategory().getLabel(),
                place.getDetailCategory(),
                place.getRoadAddress(),
                place.getLongitude(),
                place.getLatitude(),
                place.getImageUrl(),
                place.getPetAccessType(),
                place.getPetRequirements()
        );
    }
}
