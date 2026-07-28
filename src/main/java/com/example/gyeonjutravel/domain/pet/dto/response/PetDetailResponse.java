package com.example.gyeonjutravel.domain.pet.dto.response;

import com.example.gyeonjutravel.domain.pet.entity.Pet;
import com.example.gyeonjutravel.domain.pet.entity.enums.DogSize;
import com.example.gyeonjutravel.domain.pet.entity.enums.PetGender;
import com.example.gyeonjutravel.domain.pet.entity.enums.PetPersonality;

public record PetDetailResponse(
        Long petId,
        String name,
        String profileImageUrl,
        String breed,
        DogSize size,
        Integer age,
        PetGender gender,
        PetPersonality personality
) {

    public static PetDetailResponse from(Pet pet) {
        return new PetDetailResponse(
                pet.getId(),
                pet.getName(),
                pet.getProfileImageUrl(),
                pet.getBreed(),
                pet.getSize(),
                pet.getAge(),
                pet.getGender(),
                pet.getPersonality()
        );
    }
}
