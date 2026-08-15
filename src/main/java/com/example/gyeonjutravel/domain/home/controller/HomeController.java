package com.example.gyeonjutravel.domain.home.controller;

import com.example.gyeonjutravel.domain.home.dto.response.HomeResponse;
import com.example.gyeonjutravel.domain.stamp.service.StampService;
import com.example.gyeonjutravel.global.apiPayload.ApiResponse;
import com.example.gyeonjutravel.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
@Tag(name = "홈", description = "홈 화면 API")
public class HomeController {

    private final StampService stampService;

    @Operation(
            summary = "홈 화면 조회",
            description = "대표 반려견, 발자국 개수, 획득 스탬프 3개, 관광지 6개를 조회합니다."
    )
    @GetMapping
    public ApiResponse<HomeResponse> getHome(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(stampService.getHome(userDetails.member().getId()));
    }
}
