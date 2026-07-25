package com.example.gyeonjutravel.domain.pet.dto.response;

import com.example.gyeonjutravel.domain.pet.entity.Pet;

public record PetSummaryResponse(
        Long petId,
        String name,
        String profileImageUrl
) {

    public static PetSummaryResponse from(Pet pet) {
        return new PetSummaryResponse(
                pet.getId(),
                pet.getName(),
                pet.getProfileImageUrl()
        );
    }
}
