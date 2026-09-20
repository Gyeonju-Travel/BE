package com.example.gyeonjutravel.domain.place.config;

import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.place.entity.PlaceCategory;
import com.example.gyeonjutravel.domain.place.repository.PlaceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.place-data", name = "initialize", havingValue = "true", matchIfMissing = true)
public class PlaceDataInitializer implements ApplicationRunner {

    private final PlaceRepository placeRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        ClassPathResource resource = new ClassPathResource("data/places.json");
        PlaceSeedDocument document = objectMapper.readValue(resource.getInputStream(), PlaceSeedDocument.class);

        Set<String> existingSourceKeys = placeRepository.findAll().stream()
                .map(Place::getSourceKey)
                .collect(Collectors.toSet());
        // 기존 장소의 ID와 연결된 일정·스탬프를 유지하고, 누락된 로컬 장소만 추가.
        List<Place> additions = document.places().stream()
                .filter(seed -> !existingSourceKeys.contains(seed.sourceKey()))
                .map(PlaceSeed::toEntity)
                .toList();
        if (!additions.isEmpty()) {
            placeRepository.saveAll(additions);
        }
        log.info("로컬 장소 초기 데이터 {}건을 추가했습니다.", additions.size());
    }

    private record PlaceSeedDocument(List<PlaceSeed> places, List<SkippedPlace> skipped) {
    }

    private record SkippedPlace(String name, String reason) {
    }

    private record PlaceSeed(
            String sourceKey,
            PlaceCategory category,
            String originalCategory,
            String name,
            String area,
            String administrativeDistrict,
            String detailCategory,
            String roadAddress,
            String lotAddress,
            String postalCode,
            String phone,
            String businessHours,
            String breakTime,
            String closedDays,
            String hoursNote,
            Double longitude,
            Double latitude,
            String imageUrl,
            String petAccessType,
            String allowedPets,
            String petRequirements,
            String petInfo,
            String petFacilities,
            String petProvidedItems,
            String petSafetyInfo
    ) {
        private Place toEntity() {
            return toEntity(sourceKey);
        }

        private Place toEntity(String entitySourceKey) {
            return Place.builder()
                    .sourceKey(entitySourceKey)
                    .category(category)
                    .originalCategory(originalCategory)
                    .name(name)
                    .area(area)
                    .administrativeDistrict(administrativeDistrict)
                    .detailCategory(detailCategory)
                    .roadAddress(roadAddress)
                    .lotAddress(lotAddress)
                    .postalCode(postalCode)
                    .phone(phone)
                    .businessHours(businessHours)
                    .breakTime(breakTime)
                    .closedDays(closedDays)
                    .hoursNote(hoursNote)
                    .longitude(longitude)
                    .latitude(latitude)
                    .imageUrl(imageUrl)
                    .petAccessType(petAccessType)
                    .allowedPets(allowedPets)
                    .petRequirements(petRequirements)
                    .petInfo(petInfo)
                    .petFacilities(petFacilities)
                    .petProvidedItems(petProvidedItems)
                    .petSafetyInfo(petSafetyInfo)
                    .build();
        }
    }
}
