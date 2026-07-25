package com.example.gyeonjutravel.domain.pet.controller;

import com.example.gyeonjutravel.domain.pet.dto.request.PetOnboardingRequest;
import com.example.gyeonjutravel.domain.pet.dto.response.PetOnboardingResponse;
import com.example.gyeonjutravel.domain.pet.service.PetService;
import com.example.gyeonjutravel.global.apiPayload.ApiResponse;
import com.example.gyeonjutravel.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/onboarding")
@Tag(name = "온보딩", description = "회원가입 후 반려견 온보딩 API")
public class OnboardingController {

    private final PetService petService;

    @Operation(summary = "온보딩 완료", description = "온보딩 정보를 저장하고 대표 반려견을 등록합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PetOnboardingResponse> complete(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestPart("request") PetOnboardingRequest request,
            @RequestPart(value = "image", required = false) org.springframework.web.multipart.MultipartFile image
    ) {
        return ApiResponse.created(petService.completeOnboarding(userDetails.member().getId(), request, image));
    }
}
