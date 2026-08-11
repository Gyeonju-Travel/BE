package com.example.gyeonjutravel.domain.stamp.controller;

import com.example.gyeonjutravel.domain.stamp.dto.response.PetFootprintResponse;
import com.example.gyeonjutravel.domain.stamp.service.StampService;
import com.example.gyeonjutravel.global.apiPayload.ApiResponse;
import com.example.gyeonjutravel.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pets/{petId}/footprints")
@Tag(name = "반려견 발자국", description = "반려견이 일정에서 모은 누적 발자국 조회 API")
public class PetFootprintController {

    private final StampService stampService;

    @Operation(
            summary = "반려견 누적 발자국 조회",
            description = "해당 반려견이 참여한 일정 앨범들의 누적 이동거리와 100m당 1개로 계산한 발자국 수를 조회합니다."
    )
    @GetMapping
    public ApiResponse<PetFootprintResponse> getFootprints(
            @PathVariable Long petId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(stampService.getPetFootprints(userDetails.member().getId(), petId));
    }
}
