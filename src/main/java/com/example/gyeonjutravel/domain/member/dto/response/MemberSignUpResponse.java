package com.example.gyeonjutravel.domain.member.dto.response;

public record MemberSignUpResponse(
        Long memberId,
        String email,
        String nickname,
        String phoneNumber,
        String accessToken,
        Long accessTokenExpiresIn
) {
}
