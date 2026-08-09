package com.example.gyeonjutravel.domain.terms.dto.response;

public record TermsItemResponse(
        String code,
        String title,
        boolean required
) {
}
