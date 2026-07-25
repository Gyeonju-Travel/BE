package com.example.gyeonjutravel.domain.pet.dto.response;

import java.util.List;

public record PetListResponse(
        RepresentativePetResponse representativePet,
        List<PetSummaryResponse> otherPets
) {
}
