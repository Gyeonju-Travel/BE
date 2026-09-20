package com.example.gyeonjutravel.domain.place.service;

import com.example.gyeonjutravel.domain.home.service.*;
import com.example.gyeonjutravel.domain.home.dto.request.RecommendedRouteRequest;
import com.example.gyeonjutravel.domain.home.enums.*;
import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.member.repository.MemberRepository;
import com.example.gyeonjutravel.domain.pet.entity.Pet;
import com.example.gyeonjutravel.domain.pet.entity.enums.DogSize;
import com.example.gyeonjutravel.domain.pet.repository.PetRepository;
import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.schedule.entity.DepartureArea;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.example.gyeonjutravel.global.tourapi.TourApiClient;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"app.place-data.initialize=true",
        "spring.datasource.url=jdbc:h2:mem:mixed-recommendation;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false"})
@ActiveProfiles("test")
class MixedTourismRecommendationIntegrationTest {
    @Autowired PlaceCatalog catalog;
    @Autowired RecommendedRouteService recommendations;
    @Autowired MemberRepository members;
    @Autowired PetRepository pets;
    @Autowired ObjectMapper mapper;
    @MockitoBean TourApiClient client;
    @MockitoBean RecommendedPlaceSelector selector;

    @Test
    void generatesAndSavesRoutesFromMixedSourcesForEveryDepartureAndCondition() throws Exception {
        List<JsonNode> items = new ArrayList<>();
        try (var input = getClass().getResourceAsStream("/data/tour-attractions.json")) {
            mapper.readTree(input).forEach(items::add);
        }
        for (JsonNode item : items) {
            when(client.common(item.path("contentid").asText())).thenReturn(item);
        }
        when(client.attractions()).thenReturn(items);
        var attractions = catalog.attractions();
        assertThat(attractions).hasSize(22).extracting(Place::getName).doesNotHaveDuplicates();
        assertThat(attractions.stream().filter(p -> p.getTourContentId() == null)).isEmpty();
        when(selector.select(any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            List<Place> candidates = invocation.getArgument(4);
            assertThat(candidates).extracting(Place::getId).doesNotHaveDuplicates();
            assertThat(candidates).extracting(Place::getName).contains("경주읍성", "월정교")
                    .doesNotContain("감포항", "국민힐링파크", "경주 서출지", "경주엑스포대공원", "보문정(경주)", "신라왕경숲");
            return candidates.stream().map(Place::getId).toList();
        });
        Member member = members.save(Member.builder().email("mixed@example.com").password("encoded")
                .name("여행자").phoneNumber("010-1234-5678").build());
        pets.save(Pet.builder().member(member).name("강아지").size(DogSize.MEDIUM).representative(true).build());
        int offset = 1;
        for (DepartureArea departure : DepartureArea.values()) {
            for (DogCondition condition : DogCondition.values()) {
                var job = recommendations.create(member.getId(), new RecommendedRouteRequest(
                        departure, LocalDate.now().plusDays(offset++), condition));
                await().atMost(Duration.ofSeconds(15)).until(() ->
                        recommendations.getStatus(member.getId(), job.recommendationId()).status() != RecommendedRouteStatus.CREATING);
                var status = recommendations.getStatus(member.getId(), job.recommendationId());
                assertThat(status.status()).as("%s %s: %s", departure, condition, status.errorMessage())
                        .isEqualTo(RecommendedRouteStatus.COMPLETED);
                var result = recommendations.getResult(member.getId(), job.recommendationId());
                assertThat(result.recommendedPlaces()).extracting("placeId").doesNotHaveDuplicates();
                assertThat(result.recommendedPlaces()).allSatisfy(place -> {
                    assertThat(place.walkingDurationSeconds()).isNotNull().isNotNegative();
                    assertThat(place.walkingDistanceMeters()).isNotNull().isNotNegative();
                });
                var schedule = recommendations.createSchedule(member.getId(), job.recommendationId());
                assertThat(schedule.places()).hasSize(result.recommendedPlaces().size());
            }
        }
    }
}
