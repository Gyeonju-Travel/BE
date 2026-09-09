package com.example.gyeonjutravel.domain.pet.dto.response;

import com.example.gyeonjutravel.domain.pet.entity.Pet;

public record PetSummaryResponse(
        Long petId,
        String name,
        String profileImageUrl
) {

    public static PetSummaryResponse from(Pet pet, java.util.function.Function<String, String> imageUrl) {
        return new PetSummaryResponse(
                pet.getId(),
                pet.getName(),
                imageUrl.apply(pet.getProfileImageUrl())
        );
    }
}
