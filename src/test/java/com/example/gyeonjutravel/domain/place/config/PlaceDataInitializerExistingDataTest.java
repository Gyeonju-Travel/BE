package com.example.gyeonjutravel.domain.place.config;

import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.place.repository.PlaceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.ApplicationArguments;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlaceDataInitializerExistingDataTest {

    @Test
    void addsMissingMapOnlyPlacesToAnExistingDatabase() throws Exception {
        PlaceRepository placeRepository = mock(PlaceRepository.class);
        when(placeRepository.count()).thenReturn(81L);
        when(placeRepository.findAll()).thenReturn(List.of());
        PlaceDataInitializer initializer = new PlaceDataInitializer(placeRepository, new ObjectMapper());

        initializer.run(mock(ApplicationArguments.class));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Place>> additionsCaptor = ArgumentCaptor.forClass(List.class);
        verify(placeRepository).saveAll(additionsCaptor.capture());
        assertThat(additionsCaptor.getValue())
                .hasSize(7)
                .allSatisfy(place -> assertThat(place.getSourceKey()).startsWith("MAP_ONLY:PLACE:"));
    }
}
