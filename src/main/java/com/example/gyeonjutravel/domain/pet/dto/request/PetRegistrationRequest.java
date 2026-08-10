package com.example.gyeonjutravel.domain.pet.dto.request;

import com.example.gyeonjutravel.domain.pet.entity.enums.DogSize;
import com.example.gyeonjutravel.domain.pet.entity.enums.PetGender;
import com.example.gyeonjutravel.domain.pet.entity.enums.PetPersonality;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PetRegistrationRequest(
        @NotBlank(message = "강아지 이름은 필수입니다.")
        @Size(max = 30, message = "강아지 이름은 30자 이하여야 합니다.")
        String name,

        @NotBlank(message = "견종은 필수입니다.")
        @Size(max = 50, message = "견종은 50자 이하여야 합니다.")
        String breed,

        @NotNull(message = "강아지 크기는 필수입니다.")
        DogSize size,

        @NotNull(message = "강아지 나이는 필수입니다.")
        @Min(value = 0, message = "강아지 나이는 0 이상이어야 합니다.")
        @Max(value = 50, message = "강아지 나이는 50 이하여야 합니다.")
        Integer age,

        @NotNull(message = "강아지 성별은 필수입니다.")
        PetGender gender,

        @NotNull(message = "강아지 성향은 필수입니다.")
        @Size(min = 2, max = 2, message = "강아지 성향은 2개를 선택해야 합니다.")
        List<@NotNull PetPersonality> personality
) {
}
