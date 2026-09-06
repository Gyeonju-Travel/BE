package com.example.gyeonjutravel.domain.place.service;

import com.example.gyeonjutravel.domain.place.dto.response.MapPlacePageResponse;
import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.place.entity.PlaceCategory;
import com.example.gyeonjutravel.domain.place.repository.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(PlaceService.class)
class PlaceServiceTest {

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private PlaceService placeService;

    @BeforeEach
    void setUp() {
        placeRepository.saveAll(List.of(
                createPlace("PLACE:1", "료미", PlaceCategory.RESTAURANT, "황리단길", 129.2097, 35.8356),
                createPlace("PLACE:2", "데어벤치", PlaceCategory.CAFE, "황리단길", 129.2127, 35.8351),
                createPlace("PLACE:3", "경주 첨성대", PlaceCategory.ATTRACTION, "첨성대", 129.2185, 35.8343),
                createPlace("PLACE:4", "교촌카페", PlaceCategory.CAFE, "교촌마을", 129.2161, 35.8317)
        ));
    }

    @Test
    void categoryAndKeywordFilterReturnMatchingMapPlace() {
        MapPlacePageResponse result = placeService.search(
                List.of(PlaceCategory.CAFE), "데어", 0, 200
        );

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.places()).extracting("name").containsExactly("데어벤치");
    }

    @Test
    void noFilterReturnsAllPlaces() {
        MapPlacePageResponse result = placeService.search(null, null, 0, 200);

        assertThat(result.totalElements()).isEqualTo(4);
    }

    @Test
    void keywordTokensCanMatchAreaAndCategoryTogether() {
        MapPlacePageResponse result = placeService.search(null, "황리단길 카페", 0, 200);

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.places()).extracting("name").containsExactly("데어벤치");
    }

    @Test
    void detailContainsPetFriendlyInformation() {
        Long placeId = placeRepository.findAll().getFirst().getId();

        assertThat(placeService.getDetail(placeId).petRequirements()).isEqualTo("리드줄");
    }

    private Place createPlace(
            String sourceKey,
            String name,
            PlaceCategory category,
            String area,
            double longitude,
            double latitude
    ) {
        return Place.builder()
                .sourceKey(sourceKey)
                .category(category)
                .originalCategory(category.getLabel())
                .name(name)
                .area(area)
                .roadAddress("경북 경주시 테스트로 1")
                .longitude(longitude)
                .latitude(latitude)
                .petAccessType("동반가능")
                .petRequirements("리드줄")
                .build();
    }
}
