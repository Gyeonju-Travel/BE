package com.example.gyeonjutravel.domain.pet.controller;

import com.example.gyeonjutravel.domain.pet.dto.request.PetCreateRequest;
import com.example.gyeonjutravel.domain.pet.dto.request.PetProfileUpdateRequest;
import com.example.gyeonjutravel.domain.pet.dto.response.PetCreateResponse;
import com.example.gyeonjutravel.domain.pet.dto.response.PetDetailResponse;
import com.example.gyeonjutravel.domain.pet.service.PetService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pets")
@Tag(name = "반려견", description = "반려견 온보딩 및 프로필 API")
public class PetController {

    private final PetService petService;

    @Operation(
            summary = "반려견 등록",
            description = "온보딩에서 입력한 반려견과 여행 취향 정보를 등록합니다."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PetCreateResponse> create(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PetCreateRequest request
    ) {
        return ApiResponse.created(petService.create(userDetails.member().getId(), request));
    }

    @Operation(summary = "내 반려견 목록 조회")
    @GetMapping
    public ApiResponse<List<PetDetailResponse>> getMyPets(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(petService.getMyPets(userDetails.member().getId()));
    }

    @Operation(summary = "반려견 상세 조회")
    @GetMapping("/{petId}")
    public ApiResponse<PetDetailResponse> get(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long petId
    ) {
        return ApiResponse.ok(petService.get(userDetails.member().getId(), petId));
    }

    @Operation(summary = "반려견 프로필 수정")
    @PatchMapping("/{petId}")
    public ApiResponse<PetDetailResponse> updateProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long petId,
            @Valid @RequestBody PetProfileUpdateRequest request
    ) {
        return ApiResponse.ok(petService.updateProfile(userDetails.member().getId(), petId, request));
    }
}
