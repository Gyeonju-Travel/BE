package com.example.gyeonjutravel.domain.report.controller;

import com.example.gyeonjutravel.domain.report.dto.request.PlaceReportCreateRequest;
import com.example.gyeonjutravel.domain.report.dto.response.PlaceReportCreateResponse;
import com.example.gyeonjutravel.domain.report.service.PlaceReportService;
import com.example.gyeonjutravel.global.apiPayload.ApiResponse;
import com.example.gyeonjutravel.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/place-reports")
@Tag(name = "장소 제보", description = "새로운 반려동물 동반 장소 제보 API")
public class PlaceReportController {

    private final PlaceReportService placeReportService;

    @Operation(summary = "장소 제보", description = "장소 정보와 선택 사진을 첨부하여 새로운 장소를 제보합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PlaceReportCreateResponse> create(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody(content = @Content(encoding = @Encoding(
                    name = "request",
                    contentType = MediaType.APPLICATION_JSON_VALUE
            )))
            @Valid @RequestPart("request") PlaceReportCreateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ApiResponse.created(placeReportService.create(userDetails.member().getId(), request, image));
    }
}
