package com.example.gyeonjutravel.domain.place.service;

import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.place.exception.PlaceErrorCode;
import com.example.gyeonjutravel.domain.place.repository.PlaceRepository;
import com.example.gyeonjutravel.domain.stamp.entity.StampType;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TourPlaceRegistry {
    private final PlaceRepository repository;
    private final TransactionTemplate transaction;

    public TourPlaceRegistry(PlaceRepository repository, PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    // API 호출이 끝난 뒤 식별자와 서비스의 스탬프 대상 분류만 저장합니다.
    public List<Place> register(List<JsonNode> items) {
        if (items.isEmpty()) return List.of();
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                return transaction.execute(status -> registerIdentities(items));
            } catch (DataIntegrityViolationException exception) {
                // 빈 DB에 동일한 contentId가 동시에 등록되면 새 트랜잭션에서 기존 행을 다시 조회합니다.
                if (attempt == 2) throw exception;
            }
        }
        throw new IllegalStateException("관광지 식별자 등록 실패");
    }

    private List<Place> registerIdentities(List<JsonNode> items) {
        Map<String, Place> byContent = new HashMap<>();
        repository.lockAttractions().forEach(place -> byContent.put(place.getTourContentId(), place));
        List<Place> result = new ArrayList<>();
        for (JsonNode item : items) {
            String contentId = item.path("contentid").asText();
            if (!contentId.matches("[0-9]+")) throw new GeneralException(PlaceErrorCode.TOUR_API_UNAVAILABLE);
            Place details = TourPlaceMapper.map(item, null, null);
            Place identity = byContent.get(contentId);
            if (identity == null) {
                identity = Place.tourReference("TOUR_API:" + contentId);
                identity.linkTourContent(contentId);
            }
            // 최초 분류 이후에는 API 명칭이 바뀌어도 같은 콘텐츠의 스탬프를 유지합니다.
            StampType.fromPlace(details).ifPresent(identity::assignStampType);
            identity = repository.save(identity);
            byContent.put(contentId, identity);
            result.add(identity);
        }
        return result;
    }
}
