package com.example.gyeonjutravel.domain.inquiry.dto.response;

import com.example.gyeonjutravel.domain.inquiry.entity.Inquiry;
import com.example.gyeonjutravel.domain.inquiry.entity.InquiryStatus;

import java.time.LocalDateTime;

public record InquiryCreateResponse(
        Long inquiryId,
        InquiryStatus status,
        LocalDateTime submittedAt
) {
    public static InquiryCreateResponse from(Inquiry inquiry) {
        return new InquiryCreateResponse(inquiry.getId(), inquiry.getStatus(), inquiry.getCreatedAt());
    }
}
