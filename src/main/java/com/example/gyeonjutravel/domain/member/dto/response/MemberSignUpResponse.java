package com.example.gyeonjutravel.domain.member.dto.response;

import java.time.LocalDate;

public record MemberSignUpResponse(
        Long memberId,
        String email,
        String name,
        LocalDate birthDate,
        String phoneNumber,
        String accessToken,
        Long accessTokenExpiresIn,
        boolean onboardingCompleted
) {
}
