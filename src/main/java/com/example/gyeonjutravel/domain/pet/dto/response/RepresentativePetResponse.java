package com.example.gyeonjutravel.domain.pet.dto.response;

import com.example.gyeonjutravel.domain.pet.entity.Pet;
import com.example.gyeonjutravel.domain.pet.entity.enums.DogSize;

public record RepresentativePetResponse(
        Long petId,
        String name,
        String profileImageUrl,
        String breed,
        DogSize size,
        Integer age
) {

    public static RepresentativePetResponse from(Pet pet, java.util.function.Function<String, String> imageUrl) {
        return new RepresentativePetResponse(
                pet.getId(),
                pet.getName(),
                imageUrl.apply(pet.getProfileImageUrl()),
                pet.getBreed(),
                pet.getSize(),
                pet.getAge()
        );
    }
}
