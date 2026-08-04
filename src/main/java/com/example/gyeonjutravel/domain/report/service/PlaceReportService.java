package com.example.gyeonjutravel.domain.report.service;

import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.member.exception.MemberErrorCode;
import com.example.gyeonjutravel.domain.member.repository.MemberRepository;
import com.example.gyeonjutravel.domain.report.dto.request.PlaceReportCreateRequest;
import com.example.gyeonjutravel.domain.report.dto.response.PlaceReportCreateResponse;
import com.example.gyeonjutravel.domain.report.entity.PlaceReport;
import com.example.gyeonjutravel.domain.report.entity.enums.PlaceReportStatus;
import com.example.gyeonjutravel.domain.report.repository.PlaceReportRepository;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.example.gyeonjutravel.global.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceReportService {
    private final MemberRepository memberRepository;
    private final PlaceReportRepository placeReportRepository;
    private final ImageStorageService imageStorageService;

    @Transactional
    public PlaceReportCreateResponse create(Long memberId, PlaceReportCreateRequest request, MultipartFile image) {
        PlaceReport report = placeReportRepository.save(PlaceReport.builder()
                .member(findMember(memberId))
                .placeName(request.placeName().trim())
                .address(request.address().trim())
                .petPolicies(request.petPolicies())
                .imageUrl(imageStorageService.upload(image, "place-reports"))
                .recommendationReason(trimToNull(request.recommendationReason()))
                .status(PlaceReportStatus.SUBMITTED)
                .build());
        return PlaceReportCreateResponse.from(report);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
