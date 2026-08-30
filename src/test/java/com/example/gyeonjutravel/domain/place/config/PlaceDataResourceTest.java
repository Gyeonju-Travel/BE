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

            assertThat(places.size()).isEqualTo(81);
            for (int index = 0; index < places.size(); index++) {
                JsonNode place = places.get(index);
                assertThat(place.path("latitude").asDouble()).isBetween(35.0, 37.0);
                assertThat(place.path("longitude").asDouble()).isBetween(128.0, 130.0);
                assertThat(place.path("sourceKey").asText()).isEqualTo("PLACE:" + (index + 1));
                assertThat(sourceKeys.add(place.path("sourceKey").asText())).isTrue();
            }
            assertThat(document.path("skipped").get(0).path("name").asText()).isEqualTo("황성공원");
        }
    }
}
