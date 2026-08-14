package com.example.gyeonjutravel.domain.stamp.dto.response;

import com.example.gyeonjutravel.domain.stamp.entity.PlaceVisit;
import com.example.gyeonjutravel.domain.stamp.entity.StampAlbum;
import com.example.gyeonjutravel.domain.stamp.entity.StampType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
        stampNames.add(StampType.WELCOME_DOG.getDisplayName());
        visits.stream()
                .map(PlaceVisit::getPlace)
                .map(StampType::fromPlace)
                .flatMap(java.util.Optional::stream)
                .map(StampType::getDisplayName)
                .forEach(stampNames::add);
        if (StampType.qualifiesForMaster(stampNames.size())) {
            stampNames.add(StampType.GYEONGJU_MASTER.getDisplayName());
        }

        return new StampAlbumResponse(
                album.getSchedule().getId(),
                album.getSchedule().getTravelDate(),
                album.getPet().getId(),
                album.getPet().getName(),
                album.getPet().getProfileImageUrl(),
                album.getFootprintCount(),
                album.getTotalDistanceMeters(),
                stampNames,
                album.getPhotos().stream()
                        .map(photo -> photo.getImageUrl())
                        .toList(),
                visits.stream()
                        .map(VisitedPlaceResponse::from)
                        .toList()
        );
    }
}
