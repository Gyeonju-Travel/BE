package com.example.gyeonjutravel.domain.place.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceDataResourceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void integratedMapDataHasValidCoordinatesAndUniqueSourceKeys() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/data/places.json")) {
            assertThat(input).isNotNull();
            JsonNode document = objectMapper.readTree(input);
            JsonNode places = document.path("places");
            Set<String> sourceKeys = new HashSet<>();

            assertThat(places.size()).isEqualTo(65);
            Set<String> apiNames = Set.of("감포항", "경주 서출지", "경주엑스포대공원", "경주읍성",
                    "국민힐링파크", "보문정(경주)", "신라왕경숲");
            for (int index = 0; index < places.size(); index++) {
                JsonNode place = places.get(index);
                assertThat(place.path("name").asText()).isNotIn(apiNames);
                assertThat(place.path("sourceKey").asText()).isEqualTo("PLACE:" + (index + 1));
                assertThat(place.path("latitude").asDouble()).isBetween(35.0, 37.0);
                assertThat(place.path("longitude").asDouble()).isBetween(128.0, 130.0);
                assertThat(place.path("category").asText()).isIn("CAFE", "RESTAURANT");
                assertThat(sourceKeys.add(place.path("sourceKey").asText())).isTrue();
            }
            assertThat(document.path("skipped").get(0).path("name").asText()).isEqualTo("황성공원");
        }
    }
}
