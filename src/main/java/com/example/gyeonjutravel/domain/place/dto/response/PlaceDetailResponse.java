package com.example.gyeonjutravel.domain.place.dto.response;

import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.place.entity.PlaceCategory;
import com.fasterxml.jackson.annotation.JsonInclude;

public record PlaceDetailResponse(
        Long id,
        String name,
        PlaceCategory category,
        String categoryLabel,
        String detailCategory,
        String area,
        String administrativeDistrict,
        String roadAddress,
        String lotAddress,
        String postalCode,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String phone,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String businessHours,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String breakTime,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String closedDays,
        Double longitude,
        Double latitude,
        String imageUrl,
        String petAccessType,
        String allowedPets,
        String petRequirements,
        String petInfo,
        String petFacilities,
        String petProvidedItems,
        String petSafetyInfo,
        String overview
) {
    public static PlaceDetailResponse from(Place place) {
        return new PlaceDetailResponse(
                place.getId(), place.getName(), place.getCategory(), place.getCategory().getLabel(),
                place.getDetailCategory(), place.getArea(), place.getAdministrativeDistrict(),
                place.getRoadAddress(), place.getLotAddress(), place.getPostalCode(),
                place.getCategory() == PlaceCategory.ATTRACTION ? null : place.getPhone(),
                place.getCategory() == PlaceCategory.ATTRACTION ? null : place.getBusinessHours(),
                place.getCategory() == PlaceCategory.ATTRACTION ? null : place.getBreakTime(),
                place.getCategory() == PlaceCategory.ATTRACTION ? null : place.getClosedDays(), place.getLongitude(),
                place.getLatitude(), place.getImageUrl(), place.getPetAccessType(), place.getAllowedPets(),
                place.getPetRequirements(), place.getPetInfo(), place.getPetFacilities(),
                place.getPetProvidedItems(), place.getPetSafetyInfo(), place.getOverview()
        );
    }
}
