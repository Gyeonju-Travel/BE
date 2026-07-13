package com.example.gyeonjutravel.domain.place.controller;

import com.example.gyeonjutravel.domain.place.dto.response.MapPlacePageResponse;
import com.example.gyeonjutravel.domain.place.dto.response.PlaceCategoryResponse;
import com.example.gyeonjutravel.domain.place.dto.response.PlaceDetailResponse;
import com.example.gyeonjutravel.domain.place.entity.PlaceCategory;
import com.example.gyeonjutravel.domain.place.service.PlaceService;
import com.example.gyeonjutravel.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places")
@Tag(name = "지도 장소", description = "반려동물 동반 장소 지도 조회 API")
public class PlaceController {

    private final PlaceService placeService;

    @Operation(summary = "지도 장소 조회", description = "카테고리, 검색어로 핀과 목록에 필요한 장소를 조회합니다.")
    @GetMapping
    public ApiResponse<MapPlacePageResponse> search(
            @RequestParam(required = false) List<PlaceCategory> categories,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(placeService.search(
                categories, keyword, page, size
        ));
    }

    @Operation(summary = "장소 상세 조회")
    @GetMapping("/{placeId}")
    public ApiResponse<PlaceDetailResponse> getDetail(@PathVariable Long placeId) {
        return ApiResponse.ok(placeService.getDetail(placeId));
    }

}
