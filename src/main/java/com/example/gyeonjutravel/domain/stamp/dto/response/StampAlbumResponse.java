package com.example.gyeonjutravel.domain.stamp.dto.response;

import com.example.gyeonjutravel.domain.stamp.entity.StampAlbum;
import com.example.gyeonjutravel.domain.stamp.entity.PlaceVisit;

import java.time.LocalDate;
import java.util.List;

public record StampAlbumResponse(
        Long scheduleId,
        LocalDate date,
        Long petId,
        String petName,
        String petProfileImageUrl,
        int footprintCount,
        long totalDistanceMeters,
        int stampCount,
        List<String> photoUrls,
        List<VisitedPlaceResponse> visitedPlaces
) {
    public static StampAlbumResponse from(StampAlbum album, List<PlaceVisit> visits) {
        return new StampAlbumResponse(
                album.getSchedule().getId(),
                album.getSchedule().getTravelDate(),
                album.getPet().getId(),
                album.getPet().getName(),
                album.getPet().getProfileImageUrl(),
                album.getFootprintCount(),
                album.getTotalDistanceMeters(),
                1 + visits.size(),
                album.getPhotos().stream()
                        .map(photo -> photo.getImageUrl())
                        .toList(),
                visits.stream()
                        .map(VisitedPlaceResponse::from)
                        .toList()
        );
    }
}
