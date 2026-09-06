package com.example.gyeonjutravel.domain.member.dto.response;

public record TokenRefreshResponse(
        String accessToken,
        String refreshToken,
        Long accessTokenExpiresIn,
        Long refreshTokenExpiresIn
) {
}
