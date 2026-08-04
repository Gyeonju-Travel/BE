package com.example.gyeonjutravel.domain.inquiry.controller;

import com.example.gyeonjutravel.domain.inquiry.dto.request.InquiryCreateRequest;
import com.example.gyeonjutravel.domain.inquiry.dto.response.InquiryCreateResponse;
import com.example.gyeonjutravel.domain.inquiry.service.InquiryService;
import com.example.gyeonjutravel.global.apiPayload.ApiResponse;
import com.example.gyeonjutravel.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inquiries")
@Tag(name = "문의", description = "고객 문의 접수 API")
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "문의 접수", description = "제목과 문의 내용을 입력하여 문의를 접수합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InquiryCreateResponse> create(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody InquiryCreateRequest request
    ) {
        return ApiResponse.created(inquiryService.create(userDetails.member().getId(), request));
    }
}
