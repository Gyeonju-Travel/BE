package com.example.gyeonjutravel.domain.place.controller;

import com.example.gyeonjutravel.domain.place.dto.request.DeleteBookmarksRequest;
import com.example.gyeonjutravel.domain.place.dto.response.MapPlacePageResponse;
import com.example.gyeonjutravel.domain.place.dto.response.MapPlaceResponse;
import com.example.gyeonjutravel.domain.place.dto.response.PlaceDetailResponse;
import com.example.gyeonjutravel.domain.place.entity.PlaceCategory;
import com.example.gyeonjutravel.domain.place.service.PlaceService;
import com.example.gyeonjutravel.global.apiPayload.ApiResponse;
import com.example.gyeonjutravel.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
@Tag(name = "장소", description = "반려동물 동반 장소 API")
public class PlaceController {

    private final PlaceService placeService;

    @Operation(summary = "장소 검색", description = "카테고리와 검색어로 장소를 조회합니다.")
    @GetMapping
    public ApiResponse<MapPlacePageResponse> search(
            @RequestParam(required = false) List<PlaceCategory> categories,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(placeService.search(categories, keyword, page, size));
    }

    @Operation(summary = "장소 상세 조회")
    @GetMapping("/{placeId}")
    public ApiResponse<PlaceDetailResponse> getDetail(@PathVariable Long placeId) {
        return ApiResponse.ok(placeService.getDetail(placeId));
    }

    @Operation(summary = "장소 저장")
    @PostMapping("/{placeId}/bookmarks")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MapPlaceResponse> saveBookmark(
            @PathVariable Long placeId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.created(placeService.saveBookmark(userDetails.member(), placeId));
    }

    @Operation(summary = "저장한 장소 조회")
    @GetMapping("/bookmarks")
    public ApiResponse<List<MapPlaceResponse>> getBookmarks(
            @RequestParam(required = false) List<PlaceCategory> categories,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(placeService.getBookmarks(userDetails.member(), categories));
    }

    @Operation(summary = "저장한 장소 삭제")
    @DeleteMapping("/bookmarks")
    public ApiResponse<Void> deleteBookmarks(
            @Valid @RequestBody DeleteBookmarksRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        placeService.deleteBookmarks(userDetails.member(), request.placeIds());
        return ApiResponse.deleted();
    }
}
