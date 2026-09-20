package com.example.gyeonjutravel.domain.place.service;

import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.place.entity.PlaceCategory;
import com.example.gyeonjutravel.domain.place.dto.response.PlaceDetailResponse;
import com.example.gyeonjutravel.domain.place.repository.PlaceRepository;
import com.example.gyeonjutravel.domain.stamp.entity.StampType;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.example.gyeonjutravel.global.tourapi.TourApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
@Import({PlaceCatalog.class, TourPlaceRegistry.class, ObjectMapper.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PlaceCatalogIntegrationTest {
    @Autowired PlaceRepository repository;
    @Autowired PlaceCatalog catalog;
    @Autowired ObjectMapper mapper;
    @MockitoBean TourApiClient client;

    @BeforeEach
    void setup() { repository.deleteAll(); }

    @Test
    void createsIdentityFromContentIdAndNeverPersistsTourismResponse() throws Exception {
        var common = mapper.readTree("""
                {"contentid":"123","contenttypeid":"12","title":"경주 첨성대","overview":"실시간 관광지 설명",
                 "mapx":"129.2185644826","mapy":"35.8343745291","addr1":"실시간 주소","firstimage":"https://example.com/new.jpg"}
                """);
        when(client.attractions()).thenReturn(List.of(common));
        when(client.common("123")).thenReturn(common);
        when(client.pet("123")).thenReturn(mapper.readTree("{\"acmpyNeedMtr\":\"이동장 필수\"}"));
        when(client.intro("123")).thenReturn(mapper.readTree("{\"usetime\":\"09:00~18:00\"}"));
        Place live = catalog.attractions().getFirst();
        Long id = live.getId();
        assertThat(live.getSourceKey()).isEqualTo("TOUR_API:123");
        assertThat(live.getStampType()).isEqualTo(StampType.CHEOMSEONGDAE);
        catalog.resolve(live);
        assertThat(live.getPetRequirements()).isEqualTo("이동장 필수");
        assertThat(PlaceDetailResponse.from(live).overview()).isEqualTo("실시간 관광지 설명");
        repository.saveAndFlush(live);
        Place stored = repository.findById(id).orElseThrow();
        assertThat(stored.getName()).isEqualTo("관광지 정보 조회 필요");
        assertThat(stored.getLongitude()).isZero();
        assertThat(stored.getPetRequirements()).isNull();
        assertThat(stored.getOverview()).isNull();
        assertThat(stored.getImageUrl()).isNull();
        assertThat(catalog.attractions().getFirst().getId()).isEqualTo(id);
        assertThat(repository.count()).isEqualTo(1);
        verify(client, times(2)).attractions();
    }

    @Test
    void localOnlyRequestsDoNotCallTourApi() {
        repository.saveAndFlush(Place.builder().sourceKey("LOCAL:1").category(PlaceCategory.CAFE)
                .originalCategory("카페").name("카페").roadAddress("경주").longitude(129.2).latitude(35.8).build());
        var local = catalog.all(false);
        assertThat(local).hasSize(1);
        catalog.resolveAll(local);
        verifyNoInteractions(client);
    }

    @Test
    void missingContentIdDoesNotAttemptLegacyMatching() {
        Place invalid = repository.saveAndFlush(Place.tourReference("TOUR_API:invalid"));
        assertThatThrownBy(() -> catalog.resolve(invalid)).isInstanceOf(GeneralException.class);
        verifyNoInteractions(client);
    }

    @Test
    void allSixStampTargetsAreClassifiedAndClassificationSurvivesTitleChange() throws Exception {
        var names = StampType.stampPlaceNames();
        for (int index = 0; index < names.size(); index++) {
            String id = String.valueOf(100 + index);
            var item = mapper.createObjectNode().put("contentid", id).put("title", names.get(index))
                    .put("mapx", "129.21").put("mapy", "35.83");
            when(client.attractions()).thenReturn(List.of(item));
            Place place = catalog.attractions().getFirst();
            StampType original = place.getStampType();
            assertThat(original).isNotNull();
            item.put("title", "변경된 이름");
            assertThat(catalog.attractions().getFirst().getStampType()).isEqualTo(original);
        }
        assertThat(repository.count()).isEqualTo(6);
    }
}
