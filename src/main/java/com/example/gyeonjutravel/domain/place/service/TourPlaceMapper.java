package com.example.gyeonjutravel.domain.place.service;

import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.place.entity.PlaceCategory;
import com.example.gyeonjutravel.domain.place.exception.PlaceErrorCode;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.fasterxml.jackson.databind.JsonNode;

public final class TourPlaceMapper {
    private TourPlaceMapper() {}

    public static Place map(JsonNode common, JsonNode intro, JsonNode pet) {
        Double longitude = coordinate(common, "mapx", -180, 180);
        Double latitude = coordinate(common, "mapy", -90, 90);
        if (text(common, "title") == null || longitude == null || latitude == null) {
            throw new GeneralException(PlaceErrorCode.TOUR_API_UNAVAILABLE);
        }
        return Place.builder().category(PlaceCategory.ATTRACTION).originalCategory("관광지")
                .name(text(common, "title")).overview(text(common, "overview"))
                .roadAddress(join(text(common, "addr1"), text(common, "addr2")))
                .postalCode(text(common, "zipcode"))
                .phone(first(text(common, "tel"), text(intro, "infocenter")))
                .longitude(longitude).latitude(latitude)
                .imageUrl(first(text(common, "firstimage"), text(common, "firstimage2")))
                .businessHours(text(intro, "usetime")).closedDays(text(intro, "restdate"))
                .petAccessType(first(text(pet, "acmpyTypeCd"), text(intro, "chkpet")))
                .allowedPets(text(pet, "acmpyPsblCpam"))
                .petRequirements(text(pet, "acmpyNeedMtr"))
                .petInfo(text(pet, "etcAcmpyInfo"))
                .petFacilities(text(pet, "relaPosesFclty"))
                .petProvidedItems(text(pet, "relaFrnshPrdlst"))
                .petSafetyInfo(text(pet, "relaAcdntRiskMtr"))
                .build();
    }

    private static String text(JsonNode node, String field) {
        if (node == null) return null;
        String value = node.path(field).asText("").trim();
        return value.isBlank() ? null : value;
    }
    private static String first(String first, String second) { return first != null ? first : second; }
    private static String join(String first, String second) {
        return first == null ? second : second == null ? first : first + " " + second;
    }
    private static Double coordinate(JsonNode node, String field, double min, double max) {
        try {
            double value = Double.parseDouble(node.path(field).asText());
            return Double.isFinite(value) && value != 0 && value >= min && value <= max ? value : null;
        } catch (NumberFormatException exception) { return null; }
    }
}
