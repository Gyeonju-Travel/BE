package com.example.gyeonjutravel.domain.stamp.controller;

import com.example.gyeonjutravel.domain.stamp.dto.request.PlaceVisitCreateRequest;
import com.example.gyeonjutravel.domain.stamp.dto.response.PlaceVisitResponse;
import com.example.gyeonjutravel.domain.stamp.service.StampService;
import com.example.gyeonjutravel.global.apiPayload.ApiResponse;
import com.example.gyeonjutravel.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places/{placeId}/visits")
@Tag(name = "관광지 방문", description = "관광지 40m 이내 도착을 검증하고 스탬프 방문 기록을 저장하는 API")
public class PlaceVisitController {

    private final StampService stampService;

    @Operation(
            summary = "관광지 방문 체크",
            description = "현재 좌표와 관광지 좌표의 거리가 40m 이내이면 방문으로 인정합니다. 스탬프 개수는 웰컴 1개와 일정 내 distinct 방문 관광지 수로 계산합니다."
    )
    @PostMapping
    public ApiResponse<PlaceVisitResponse> visit(
            @PathVariable Long placeId,
            @Valid @RequestBody PlaceVisitCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(stampService.visitPlace(userDetails.member().getId(), placeId, request));
    }
}
