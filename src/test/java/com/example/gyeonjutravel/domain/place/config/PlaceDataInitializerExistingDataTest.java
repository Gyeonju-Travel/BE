package com.example.gyeonjutravel.domain.place.config;

import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.place.entity.PlaceCategory;
import com.example.gyeonjutravel.domain.place.repository.PlaceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.ApplicationArguments;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PlaceDataInitializerExistingDataTest {
    @Test
    void existingDatabaseDoesNotRecreateLocalAttractions() throws Exception {
        PlaceRepository repository = mock(PlaceRepository.class);
        List<Place> existing = new ArrayList<>();
        for (int i = 1; i <= 65; i++) {
            existing.add(Place.builder().sourceKey("PLACE:" + i).category(PlaceCategory.CAFE).build());
        }
        Place api = Place.tourReference("TOUR_API:2756611");
        api.linkTourContent("2756611");
        existing.add(api);
        when(repository.findAll()).thenAnswer(ignored -> List.copyOf(existing));
        PlaceDataInitializer initializer = new PlaceDataInitializer(repository, new ObjectMapper());
        initializer.run(mock(ApplicationArguments.class));
        verify(repository, never()).saveAll(any());
        initializer.run(mock(ApplicationArguments.class));
        verify(repository, never()).saveAll(any());
    }
}
