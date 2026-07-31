package com.example.gyeonjutravel.domain.schedule.controller;

import com.example.gyeonjutravel.domain.schedule.dto.request.ScheduleCreateRequest;
import com.example.gyeonjutravel.domain.schedule.dto.request.SchedulePreviewRequest;
import com.example.gyeonjutravel.domain.schedule.dto.response.SchedulePreviewResponse;
import com.example.gyeonjutravel.domain.schedule.dto.response.ScheduleResponse;
import com.example.gyeonjutravel.domain.schedule.service.ScheduleService;
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
@RequestMapping("/api/schedules")
@Tag(name = "일정", description = "저장 장소를 이용한 도보 일정 API")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(
            summary = "일정 미리보기",
            description = "도보시간 매트릭스를 계산하고 최근접 이웃 알고리즘으로 장소를 자동 정렬합니다."
    )
    @PostMapping("/preview")
    public ApiResponse<SchedulePreviewResponse> preview(
            @Valid @RequestBody SchedulePreviewRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(scheduleService.preview(userDetails.member().getId(), request));
    }

    @Operation(
            summary = "일정 저장",
            description = "미리보기의 장소를 사용자가 확정한 순서와 도보시간으로 저장합니다."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ScheduleResponse> create(
            @Valid @RequestBody ScheduleCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.created(scheduleService.create(userDetails.member().getId(), request));
    }
}
