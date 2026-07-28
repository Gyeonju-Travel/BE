package com.example.gyeonjutravel.domain.member.dto.response;

public record PasswordResetVerificationResponse(
        String resetToken,
        Long expiresIn
) {
}
