package com.example.gyeonjutravel.domain.home.controller;

import com.example.gyeonjutravel.domain.home.dto.request.RecommendedRouteRequest;
import com.example.gyeonjutravel.domain.home.dto.response.RecommendedRouteJobResponse;
import com.example.gyeonjutravel.domain.home.dto.response.RecommendedRouteStatusResponse;
import com.example.gyeonjutravel.domain.home.service.RecommendedRouteService;
import com.example.gyeonjutravel.global.apiPayload.ApiResponse;
import com.example.gyeonjutravel.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend-routes")
@Tag(name = "추천 경로", description = "AI 추천 경로 생성 및 조회 API")
public class RecommendedRouteController {

    private final RecommendedRouteService recommendedRouteService;

    @Operation(
            summary = "추천 경로 생성 요청",
            description = "출발지, 방문 날짜, 반려견의 오늘 컨디션을 입력받아 AI 추천 경로 생성 작업을 비동기로 시작합니다. "
    )
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<RecommendedRouteJobResponse> create(
            @Valid @RequestBody RecommendedRouteRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(recommendedRouteService.create(userDetails.member().getId(), request));
    }

    @Operation(
            summary = "추천 경로 생성 상태 조회",
            description = "추천 경로 생성 요청으로 발급된 jobId의 진행 상태를 조회합니다. "
    )
    @GetMapping("/{recommendationId}")
    public ApiResponse<RecommendedRouteStatusResponse> getStatus(
            @PathVariable String recommendationId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(recommendedRouteService.getStatus(userDetails.member().getId(), recommendationId));
    }
}
