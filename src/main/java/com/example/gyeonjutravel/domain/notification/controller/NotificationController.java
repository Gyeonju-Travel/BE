package com.example.gyeonjutravel.domain.notification.controller;

import com.example.gyeonjutravel.domain.notification.dto.request.NotificationSettingUpdateRequest;
import com.example.gyeonjutravel.domain.notification.dto.response.NotificationListItemResponse;
import com.example.gyeonjutravel.domain.notification.dto.response.NotificationListResponse;
import com.example.gyeonjutravel.domain.notification.dto.response.NotificationResponse;
import com.example.gyeonjutravel.domain.notification.dto.response.NotificationSettingResponse;
import com.example.gyeonjutravel.domain.notification.service.NotificationService;
import com.example.gyeonjutravel.global.apiPayload.ApiResponse;
import com.example.gyeonjutravel.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Tag(name = "알림", description = "사용자 알림 API")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림 목록 조회", description = "알림 ID와 읽음 상태를 조회합니다.")
    @GetMapping
    public ApiResponse<NotificationListResponse> getNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(notificationService.getNotifications(userDetails.member().getId()));
    }

    @Operation(summary = "스크랩 알림 발송", description = "오후 9시 이후 스크랩 앨범 조회 알림을 발송합니다.")
    @PostMapping("/stamp-album/{scheduleId}")
    public ApiResponse<NotificationResponse> sendStampAlbumNotification(
            @PathVariable Long scheduleId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(notificationService.sendStampAlbumReadyNotification(
                userDetails.member().getId(),
                scheduleId
        ));
    }

    @Operation(summary = "알림 설정 조회", description = "알림 수신 설정 여부를 조회합니다.")
    @GetMapping("/setting")
    public ApiResponse<NotificationSettingResponse> getSetting(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(notificationService.getSetting(userDetails.member().getId()));
    }

    @Operation(summary = "알림 설정 변경", description = "알림 수신 여부를 변경합니다.")
    @PatchMapping("/setting")
    public ApiResponse<NotificationSettingResponse> updateSetting(
            @Valid @RequestBody NotificationSettingUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(notificationService.updateSetting(userDetails.member().getId(), request));
    }

    @Operation(summary = "알림 읽음 처리", description = "선택한 알림을 읽음 상태로 변경합니다.")
    @PatchMapping("/{notificationId}/read")
    public ApiResponse<NotificationListItemResponse> markAsRead(
            @PathVariable Long notificationId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(notificationService.markAsRead(userDetails.member().getId(), notificationId));
    }
}
