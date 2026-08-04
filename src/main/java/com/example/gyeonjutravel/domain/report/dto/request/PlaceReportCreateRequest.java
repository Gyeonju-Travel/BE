package com.example.gyeonjutravel.domain.report.dto.request;

import com.example.gyeonjutravel.domain.report.entity.enums.PetPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record PlaceReportCreateRequest(
        @NotBlank(message = "장소명은 필수입니다.")
        @Size(max = 100, message = "장소명은 100자 이하여야 합니다.")
        String placeName,

        @NotBlank(message = "주소는 필수입니다.")
        @Size(max = 255, message = "주소는 255자 이하여야 합니다.")
        String address,

        @NotEmpty(message = "반려동물 입장 조건을 하나 이상 선택해 주세요.")
        Set<PetPolicy> petPolicies,

        @Size(max = 1000, message = "추천 이유는 1000자 이하여야 합니다.")
        String recommendationReason
) {
}
