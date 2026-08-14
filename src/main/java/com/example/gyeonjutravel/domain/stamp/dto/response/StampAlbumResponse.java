package com.example.gyeonjutravel.domain.stamp.dto.response;

import com.example.gyeonjutravel.domain.stamp.entity.PlaceVisit;
import com.example.gyeonjutravel.domain.stamp.entity.StampAlbum;
import com.example.gyeonjutravel.domain.stamp.entity.StampType;

import java.time.LocalDate;
import java.util.ArrayList;
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
        List<String> stampNames,
        List<String> photoUrls,
        List<VisitedPlaceResponse> visitedPlaces
) {
    public static StampAlbumResponse from(StampAlbum album, List<PlaceVisit> visits) {
        List<String> stampNames = new ArrayList<>();
        stampNames.add(StampType.PERFECT_TRIP.getDisplayName());
        visits.stream()
                .map(PlaceVisit::getPlace)
                .map(StampType::fromPlace)
                .flatMap(java.util.Optional::stream)
                .map(StampType::getDisplayName)
                .forEach(stampNames::add);
        String selectedStampName = stampNames.get(ThreadLocalRandom.current().nextInt(stampNames.size()));

        return new StampAlbumResponse(
                album.getSchedule().getId(),
                album.getSchedule().getTravelDate(),
                album.getPet().getId(),
                album.getPet().getName(),
                album.getPet().getProfileImageUrl(),
                album.getFootprintCount(),
                album.getTotalDistanceMeters(),
                List.of(selectedStampName),
                album.getPhotos().stream()
                        .map(photo -> photo.getImageUrl())
                        .toList(),
                visits.stream()
                        .map(VisitedPlaceResponse::from)
                        .toList()
        );
    }
}
