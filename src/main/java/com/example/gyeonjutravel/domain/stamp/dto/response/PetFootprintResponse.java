package com.example.gyeonjutravel.domain.stamp.dto.response;

public record PetFootprintResponse(
        Long petId,
        long totalDistanceMeters,
        int footprintCount
) {
    public static PetFootprintResponse of(Long petId, long totalDistanceMeters) {
        return new PetFootprintResponse(petId, totalDistanceMeters, (int) (totalDistanceMeters / 100));
    }
}
