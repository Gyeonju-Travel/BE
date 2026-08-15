package com.example.gyeonjutravel.domain.home.dto.response;

import com.example.gyeonjutravel.domain.place.entity.Place;

public record HomePlaceResponse(
        Long placeId,
        String placeName,
        String imageUrl,
        Double longitude,
        Double latitude
) {
    public static HomePlaceResponse from(Place place) {
        return new HomePlaceResponse(
                place.getId(),
                place.getName(),
                place.getImageUrl(),
                place.getLongitude(),
                place.getLatitude()
        );
    }
}
