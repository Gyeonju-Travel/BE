package com.example.gyeonjutravel.domain.place.service;

import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.place.entity.PlaceCategory;
import com.example.gyeonjutravel.domain.place.exception.PlaceErrorCode;
import com.example.gyeonjutravel.domain.place.repository.PlaceRepository;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.example.gyeonjutravel.global.tourapi.TourApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlaceCatalog {
    private final PlaceRepository repository;
    private final TourApiClient client;
    private final TourPlaceRegistry registry;

    public List<Place> all(boolean includeAttractions) {
        List<Place> places = new ArrayList<>();
        places.addAll(repository.findAllByCategory(PlaceCategory.CAFE));
        places.addAll(repository.findAllByCategory(PlaceCategory.RESTAURANT));
        if (includeAttractions) {
            try {
                places.addAll(attractions());
            } catch (GeneralException exception) {
                // 전체 조회에서는 관광공사 API 장애가 식당·카페 목록까지 막지 않도록 함.
            }
        }
        return places;
    }

    public List<Place> attractions() {
        List<JsonNode> items = client.attractions();
        List<Place> identities = registry.register(items);
        Map<String, JsonNode> byContent = new HashMap<>();
        items.forEach(item -> byContent.put(item.path("contentid").asText(), item));
        identities.forEach(place -> place.useTourDetails(TourPlaceMapper.map(byContent.get(place.getTourContentId()), null, null)));
        List<Place> places = new ArrayList<>(repository.findAllByCategoryAndTourContentIdIsNull(PlaceCategory.ATTRACTION));
        places.addAll(identities);
        return places;
    }

    public Place resolve(Place place) {
        return resolveAll(List.of(place)).getFirst();
    }

    /** 요청 간 캐시는 사용하지 않으며, 현재 조회 작업 안에서만 동일한 ID의 중복 호출을 제거 */
    public List<Place> resolveAll(List<Place> places) {
        Map<String, Place> details = new HashMap<>();
        for (Place place : places) {
            if (place.getCategory() != PlaceCategory.ATTRACTION) continue;
            String id = place.getTourContentId();
            if (id == null) {
                if (place.getSourceKey() != null && place.getSourceKey().startsWith("TOUR_API:")) {
                    throw new GeneralException(PlaceErrorCode.PLACE_NOT_FOUND);
                }
                // 직접 등록한 관광지는 DB의 정보를 사용한다.
                continue;
            }
            final String contentId = id;
            Place data = details.computeIfAbsent(id, ignored -> TourPlaceMapper.map(
                    client.common(contentId), client.intro(contentId), client.pet(contentId)));
            place.useTourDetails(data);
        }
        return places;
    }
}
