package com.example.gyeonjutravel.domain.home.dto.response;

import com.example.gyeonjutravel.domain.pet.entity.Pet;
import com.example.gyeonjutravel.domain.pet.entity.enums.PetPersonality;
import com.example.gyeonjutravel.domain.place.entity.Place;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public record HomeResponse(
        String petName,
        String petProfileImageUrl,
        List<PetPersonality> petPersonalities,
        int footprintCount,
        List<String> stampNames,
        List<HomePlaceResponse> places
) {
    public static HomeResponse of(
            Pet pet,
            long totalDistanceMeters,
            List<String> stampNames,
            List<Place> places
    ) {
        return new HomeResponse(
                pet.getName(),
                pet.getProfileImageUrl(),
                Stream.of(pet.getPersonality(), pet.getSecondPersonality())
                        .filter(Objects::nonNull)
                        .toList(),
                (int) (totalDistanceMeters / 100),
                stampNames,
                places.stream()
                        .map(HomePlaceResponse::from)
                        .toList()
        );
    }
}
