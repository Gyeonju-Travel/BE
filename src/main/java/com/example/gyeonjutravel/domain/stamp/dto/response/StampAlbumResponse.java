package com.example.gyeonjutravel.domain.stamp.dto.response;

import com.example.gyeonjutravel.domain.stamp.entity.PlaceVisit;
import com.example.gyeonjutravel.domain.stamp.entity.StampAlbum;
import com.example.gyeonjutravel.domain.stamp.entity.StampType;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record StampAlbumResponse(
        Long scheduleId,
        LocalDate date,
        Long petId,
        String petName,
        String petProfileImageUrl,
        int footprintCount,
        long totalDistanceMeters,
        String stampName,
        List<String> photoUrls
        // List<VisitedPlaceResponse> visitedPlaces
) {
    public static StampAlbumResponse from(StampAlbum album, List<PlaceVisit> visits) {
        List<String> attractionStampNames = visits.stream()
                .map(PlaceVisit::getPlace)
                .map(StampType::fromPlace)
                .flatMap(java.util.Optional::stream)
                .map(StampType::getDisplayName)
                .distinct()
                .toList();
        String selectedStampName = attractionStampNames.isEmpty()
                ? StampType.PERFECT_TRIP.getDisplayName()
                : attractionStampNames.get(ThreadLocalRandom.current().nextInt(attractionStampNames.size()));

        return new StampAlbumResponse(
                album.getSchedule().getId(),
                album.getSchedule().getTravelDate(),
                album.getPet().getId(),
                album.getPet().getName(),
                album.getPet().getProfileImageUrl(),
                album.getFootprintCount(),
                album.getTotalDistanceMeters(),
                selectedStampName,
                album.getPhotos().stream()
                        .map(photo -> photo.getImageUrl())
                        .toList()
                // visits.stream()
                //         .map(VisitedPlaceResponse::from)
                //         .toList()
        );
    }
}
