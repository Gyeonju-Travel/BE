package com.example.gyeonjutravel.domain.schedule.controller;

import com.example.gyeonjutravel.domain.schedule.dto.request.ScheduleCreateRequest;
import com.example.gyeonjutravel.domain.schedule.dto.request.ScheduleDeleteRequest;
import com.example.gyeonjutravel.domain.schedule.dto.request.SchedulePreviewRequest;
import com.example.gyeonjutravel.domain.schedule.dto.response.ScheduleDateResponse;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedules")
@Tag(name = "일정", description = "저장 장소를 이용한 도보 일정 API")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(summary = "날짜별 일정 조회", description = "선택한 날짜의 일정과 장소별 도보 이동 정보를 조회합니다.")
    @GetMapping
    public ApiResponse<ScheduleDateResponse> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(scheduleService.getByDate(userDetails.member().getId(), date));
    }

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

    @Operation(summary = "일정 수정 미리보기", description = "변경할 출발지와 장소를 가까운 순서로 추천하고 수정용 토큰을 발급합니다.")
    @PostMapping("/{scheduleId}/preview")
    public ApiResponse<SchedulePreviewResponse> updatePreview(
            @PathVariable Long scheduleId,
            @Valid @RequestBody SchedulePreviewRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(scheduleService.updatePreview(
                userDetails.member().getId(), scheduleId, request
        ));
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

    @Operation(summary = "일정 경로 수정", description = "미리보기에서 확정한 출발지, 날짜, 장소와 순서로 일정을 수정합니다.")
    @PutMapping("/{scheduleId}")
    public ApiResponse<ScheduleResponse> update(
            @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(scheduleService.update(userDetails.member().getId(), scheduleId, request));
    }

    @Operation(summary = "일정 삭제", description = "선택한 일정을 모두 삭제합니다.")
    @DeleteMapping
    public ApiResponse<Void> delete(
            @Valid @RequestBody ScheduleDeleteRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        scheduleService.delete(userDetails.member().getId(), request);
        return ApiResponse.deleted();
    }
}
