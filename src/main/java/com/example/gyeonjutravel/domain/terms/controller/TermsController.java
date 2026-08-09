package com.example.gyeonjutravel.domain.terms.controller;

import com.example.gyeonjutravel.domain.terms.dto.request.TermsAgreementRequest;
import com.example.gyeonjutravel.domain.terms.dto.response.SignUpTermsResponse;
import com.example.gyeonjutravel.domain.terms.dto.response.TermsAgreementResponse;
import com.example.gyeonjutravel.domain.terms.service.TermsService;
import com.example.gyeonjutravel.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "약관", description = "약관 조회 및 동의 API")
public class TermsController {

    private final TermsService termsService;

    @Operation(
            summary = "약관 동의 조회",
            description = "마이페이지에서 약관 동의한 내용을 확인합니다."
    )
    @GetMapping("/my-page/terms")
    public ApiResponse<SignUpTermsResponse> getSignUpTerms() {
        return ApiResponse.ok(termsService.getSignUpTerms());
    }


    @Operation(
            summary = "약관 동의",
            description = "회원가입하기 전, 약관동의 체크를 합니다."
    )
    @PostMapping("/auth/terms/agreement")
    public ApiResponse<TermsAgreementResponse> agreeSignUpTerms(
            @Valid @RequestBody TermsAgreementRequest request
    ) {
        return ApiResponse.ok(termsService.agreeSignUpTerms(request));
    }
}
