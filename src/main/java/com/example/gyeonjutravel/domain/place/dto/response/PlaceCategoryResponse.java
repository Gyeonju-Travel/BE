package com.example.gyeonjutravel.domain.place.dto.response;

import com.example.gyeonjutravel.domain.place.entity.PlaceCategory;

public record PlaceCategoryResponse(
        PlaceCategory category,
        String label,
        long count
) {
}
