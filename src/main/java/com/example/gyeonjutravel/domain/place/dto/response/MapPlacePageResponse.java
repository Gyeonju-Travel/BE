package com.example.gyeonjutravel.domain.place.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record MapPlacePageResponse(
        List<MapPlaceResponse> places,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static MapPlacePageResponse from(Page<MapPlaceResponse> result) {
        return new MapPlacePageResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}
