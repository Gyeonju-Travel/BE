package com.example.gyeonjutravel.domain.stamp.entity;

import com.example.gyeonjutravel.domain.place.entity.Place;

import java.util.Arrays;
import java.util.Optional;

public enum StampType {
    WELCOME_DOG("환영하개", null),
    GYOCHON_VILLAGE("교촌마을", "경주 교촌마을"),
    HWANGRIDAN_GIL("황리단길", "경주 황리단길"),
    WOLJEONGGYO_BRIDGE("월정교", "월정교"),
    GYERIM_FOREST("경주 계림", "경주 계림"),
    CHEOMSEONGDAE("경주 첨성대", "경주 첨성대"),
    GYEONGJU_EUPSEONG("경주 읍성", "경주읍성"),
    PERFECT_TRIP("완벽한 여행", null),
    GYEONGJU_MASTER("경주 마스터", null);

    private static final int MASTER_REQUIRED_STAMP_COUNT = 8;

    private final String displayName;
    private final String placeName;

    StampType(String displayName, String placeName) {
        this.displayName = displayName;
        this.placeName = placeName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Optional<StampType> fromPlace(Place place) {
        return Arrays.stream(values())
                .filter(stampType -> stampType.placeName != null)
                .filter(stampType -> stampType.placeName.equals(place.getName()))
                .findFirst();
    }

    public static boolean qualifiesForMaster(int earnedStampCount) {
        return earnedStampCount >= MASTER_REQUIRED_STAMP_COUNT;
    }
}
