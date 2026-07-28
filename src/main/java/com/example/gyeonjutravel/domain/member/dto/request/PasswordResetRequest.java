package com.example.gyeonjutravel.domain.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        @NotBlank(message = "비밀번호 재설정 토큰은 필수입니다.")
        String resetToken,

        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Size(min = 8, max = 30, message = "비밀번호는 8자 이상 30자 이하여야 합니다.")
        String newPassword,

        @NotBlank(message = "새 비밀번호 확인은 필수입니다.")
        String newPasswordConfirmation
) {
}
