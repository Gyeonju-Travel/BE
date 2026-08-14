package com.example.gyeonjutravel.domain.stamp.controller;

import com.example.gyeonjutravel.domain.stamp.dto.request.FootprintAddRequest;
import com.example.gyeonjutravel.domain.stamp.dto.response.MyPageStampsResponse;
import com.example.gyeonjutravel.domain.stamp.dto.response.ScheduleFootprintResponse;
import com.example.gyeonjutravel.domain.stamp.dto.response.StampAlbumResponse;
import com.example.gyeonjutravel.domain.stamp.dto.response.TravelRecordsResponse;
import com.example.gyeonjutravel.domain.stamp.service.StampService;
import com.example.gyeonjutravel.global.apiPayload.ApiResponse;
import com.example.gyeonjutravel.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "스탬프 앨범", description = "일정의 사진, 발자국, 획득 스탬프를 조회/저장하는 API")
public class StampController {

    private final StampService stampService;

    @Operation(
            summary = "스크랩 앨범 조회",
            description = "선택한 일정의 대표 반려견 발자국 개수, 누적 거리, 사진 2장, 방문 관광지와 획득 스탬프 이름을 조회합니다."
    )
    @GetMapping("/api/schedules/{scheduleId}/stamp-album")
    public ApiResponse<StampAlbumResponse> getAlbum(
            @PathVariable Long scheduleId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(stampService.getAlbum(userDetails.member().getId(), scheduleId));
    }

    @Operation(
            summary = "발자국 이동거리 추가",
            description = "앱이 로컬에서 누적한 증가 이동거리를 일정 앨범에 더합니다. 일정 시작 시각부터 오후 9시까지만 기록할 수 있고, 100m당 발자국 1개로 계산합니다."
    )
    @PostMapping("/api/schedules/{scheduleId}/stamp-album/footprints")
    public ApiResponse<ScheduleFootprintResponse> addFootprints(
            @PathVariable Long scheduleId,
            @Valid @RequestBody FootprintAddRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(stampService.addFootprints(userDetails.member().getId(), scheduleId, request));
    }

    @Operation(
            summary = "스크랩 앨범 사진 저장",
            description = "일정 종료 화면에서 선택한 사진 2장을 저장하고, 저장된 사진 URL과 발자국 정보를 함께 반환합니다."
    )
    @PostMapping(value = "/api/schedules/{scheduleId}/stamp-album/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<StampAlbumResponse> savePhotos(
            @PathVariable Long scheduleId,
            @RequestPart("photos") List<MultipartFile> photos,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(stampService.savePhotos(userDetails.member().getId(), scheduleId, photos));
    }

    @Operation(
            summary = "스탬프 앨범",
            description = "사용자가 얻은 스탬프 목록을 조회합니다."
    )
    @GetMapping("/api/my-page/stamps")
    public ApiResponse<MyPageStampsResponse> getMyPageStamps(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(stampService.getMyPageStamps(userDetails.member().getId()));
    }

    @Operation(
            summary = "여행 기록",
            description = "일정을 종류한 후 스크랩 앨범을 마무리하면 여행 기록에 반영됩니다."
    )
    @GetMapping("/api/my-page/travel-records")
    public ApiResponse<TravelRecordsResponse> getTravelRecords(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(stampService.getTravelRecords(userDetails.member().getId()));
    }
}
