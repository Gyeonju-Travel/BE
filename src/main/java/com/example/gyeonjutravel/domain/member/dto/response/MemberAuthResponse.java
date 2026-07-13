package com.example.gyeonjutravel.domain.member.dto.response;

public record MemberAuthResponse(
        Long memberId,
        String email,
        String nickname,
        String phoneNumber,
        String tokenType,
        String accessToken,
        Long expiresIn
) {
}
