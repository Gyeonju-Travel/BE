package com.example.gyeonjutravel.domain.pet.dto.response;

import com.example.gyeonjutravel.domain.pet.entity.Pet;
import com.example.gyeonjutravel.domain.pet.entity.enums.DogSize;
import com.example.gyeonjutravel.domain.pet.entity.enums.TravelPreference;
import com.example.gyeonjutravel.domain.pet.entity.enums.WalkingStyle;

public record PetOnboardingResponse(
        Long petId,
        String name,
        String profileImageUrl,
        DogSize size,
        TravelPreference travelPreference,
        WalkingStyle walkingStyle
) {

    public static PetOnboardingResponse from(Pet pet) {
        return new PetOnboardingResponse(
                pet.getId(),
                pet.getName(),
                pet.getProfileImageUrl(),
                pet.getSize(),
                pet.getTravelPreference(),
                pet.getWalkingStyle()
        );
    }
}
