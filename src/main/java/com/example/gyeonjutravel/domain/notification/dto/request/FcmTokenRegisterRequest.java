package com.example.gyeonjutravel.domain.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FcmTokenRegisterRequest(
        @NotBlank
        @Size(max = 512)
        String token
) {
}
