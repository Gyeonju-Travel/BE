package com.example.gyeonjutravel.domain.terms.dto.response;

import java.util.List;

public record SignUpTermsResponse(
        List<TermsItemResponse> terms
) {
}
