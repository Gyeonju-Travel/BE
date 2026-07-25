package com.example.gyeonjutravel.domain.member.dto.response;

import com.example.gyeonjutravel.domain.member.entity.Gender;

import java.time.LocalDate;

public record MemberSignUpResponse(
        Long memberId,
        String email,
        String name,
        LocalDate birthDate,
        Gender gender,
        String phoneNumber,
        String accessToken,
        Long accessTokenExpiresIn,
        boolean onboardingCompleted
) {
}
