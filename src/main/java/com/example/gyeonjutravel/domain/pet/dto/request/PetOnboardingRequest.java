package com.example.gyeonjutravel.domain.pet.dto.request;

import com.example.gyeonjutravel.domain.pet.entity.enums.DogSize;
import com.example.gyeonjutravel.domain.pet.entity.enums.PetPersonality;
import com.example.gyeonjutravel.domain.pet.entity.enums.TravelPreference;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PetOnboardingRequest(
        @NotBlank(message = "강아지 이름은 필수입니다.")
        @Size(max = 30, message = "강아지 이름은 30자 이하여야 합니다.")
        String name,

        @NotNull(message = "강아지 크기는 필수입니다.")
        DogSize size,

        @NotNull(message = "여행 취향은 필수입니다.")
        TravelPreference travelPreference,

        @NotNull(message = "강아지 성향은 필수입니다.")
        PetPersonality personality
) {
}
