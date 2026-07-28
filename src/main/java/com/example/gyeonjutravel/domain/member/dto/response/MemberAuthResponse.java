package com.example.gyeonjutravel.domain.member.dto.response;

public record MemberAuthResponse(
        Long memberId,
        String accessToken,
        String refreshToken,
        Long accessTokenExpiresIn,
        Long refreshTokenExpiresIn,
        boolean onboardingCompleted
) {
}
