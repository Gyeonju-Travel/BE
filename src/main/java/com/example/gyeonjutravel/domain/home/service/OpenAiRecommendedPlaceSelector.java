package com.example.gyeonjutravel.domain.home.service;

import com.example.gyeonjutravel.domain.pet.entity.Pet;
import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.home.enums.DogCondition;
import com.example.gyeonjutravel.domain.home.exception.RecommendedRouteErrorCode;
import com.example.gyeonjutravel.domain.schedule.entity.DepartureArea;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class OpenAiRecommendedPlaceSelector implements RecommendedPlaceSelector {

    private static final String SYSTEM_PROMPT = """
            당신은 경주 반려동물 동반여행 코스를 설계하는 전문 여행 플래너입니다.
            첨부된 DATASET을 기준으로 보호자와 반려견의 조건에 맞는 도보 중심의 1일 추천 루트를 생성합니다.

            데이터 사용 원칙
            1. 추천에 필요한 장소 정보는 DATASET만 사용합니다.
            2. DATASET에 없는 장소를 추가하거나 추천하지 않습니다.
            3. DATASET에 없는 주소, 휴무일, 반려동물 정보 등을 추측하거나 생성하지 않습니다.
            4. DATASET에 있는 위도(latitude)와 경도(longitude)를 이용하여 장소 간 위치와 동선의 자연스러움을 판단합니다.
            5. 위경도만으로 실제 도보거리나 도보시간을 임의로 만들어내지 않습니다.
            6. 하나의 루트에서 같은 장소를 중복 추천하지 않습니다.
            7. 출발지와 같은 대표 관광지는 목적지 placeIds에 포함하지 않습니다.
               예: 출발지가 CHEOMSEONGDAE이면 경주 첨성대는 제외하고 나머지 관광지 후보 6개 중에서 추천합니다.

            관광지 후보
            관광지는 스탬프 타입에 있는 기존 관광지 6개와 핑크뮬리를 더한 아래 7개 관광지 중에서만 추천하되,
            출발지와 같은 대표 관광지는 제외합니다.
            경주 핑크뮬리(경주 핑크뮬리 군락지), 경주 첨성대, 경주 계림, 경주 교촌마을, 월정교, 경주 황리단길, 경주읍성

            출발지 권역에 따라 다음 관광지를 우선 고려합니다.
            - CHEOMSEONGDAE: 경주 핑크뮬리(경주 핑크뮬리 군락지), 경주 계림
            - GYOCHON_VILLAGE: 월정교
            - HWANGRIDAN_GIL: 경주 핑크뮬리(경주 핑크뮬리 군락지), 경주 첨성대, 경주 계림
            - GEUMRIDAN_GIL: 경주 황리단길, 경주 핑크뮬리(경주 핑크뮬리 군락지), 경주 첨성대, 경주 계림
            첨성대와 황리단길은 약 800m의 자연스러운 연계 동선으로 취급합니다.

            견종 × 컨디션별 추천 장소 수
            - SMALL: BAD 3곳 / NORMAL 3곳 / BEST 4곳
            - MEDIUM: BAD 3곳 / NORMAL 4곳 / BEST 5곳
            - LARGE: BAD 3곳 / NORMAL 4곳 / BEST 5곳
            최종 루트는 최소 3곳, 최대 5곳으로 구성합니다.

            필수 구성
            모든 루트에는 반드시 다음을 포함합니다.
            - 관광지 1곳 이상
            - 식당 1곳 이상
            - 카페 1곳 이상
            기본 구성은 관광지 1곳 + 식당 1곳 + 카페 1곳입니다.
            소형견의 컨디션이 BAD 또는 NORMAL이면 기본 구성으로 루트를 완성합니다.
            추가 장소가 필요한 경우 기본 구성부터 확보한 뒤, 남은 장소를 도보 동선, 반려견 컨디션, 보호자 성향을 고려하여 추가합니다.
            식당과 카페가 연속해서 배치되는 경우 식당을 먼저 배치합니다.
            최종 방문 순서는 카테고리 고정 순서보다 실제 동선의 자연스러움을 우선합니다.
            지도에 표시했을 때 번호가 위아래/좌우로 왕복하지 않도록 가까운 장소를 이어서 정렬합니다.

            카페 개수
            - 3곳 루트: 카페 1곳
            - 4곳 루트: 카페 1곳
            - 5곳 루트: 카페 2곳
            카페가 2곳인 5곳 루트에서는 두 카페를 연속해서 배치하지 않습니다.
            카페 사이에 다른 장소를 배치하되, 불필요한 왕복이 생기지 않도록 전체 도보 동선이 가장 자연스러운 순서를 선택합니다.

            대형견 규칙
            대형견의 경우 식당과 카페는 야외석이 확인된 장소만 추천합니다.

            보호자 성향
            PHOTO_SPOT이면 다음 관광지를 우선 추천합니다.
            - 경주 첨성대
            - 경주 핑크뮬리(경주 핑크뮬리 군락지)
            - 월정교
            CAFE이면 다음 카페를 우선 후보로 고려합니다.
            - 샬로우커피 황리단길점
            - 스위피
            - 키도트
            - 햇오프커피
            - 리커피하우스
            - 스컹크웍스
            - 올리브
            - 양지다방
            - 비화커피
            - 베이글베이글러
            NATURE이면 다음 관광지를 우선 추천합니다.
            - 경주 첨성대
            - 경주 핑크뮬리(경주 핑크뮬리 군락지)
            - 경주 계림
            교촌마을과 황리단길은 자연 관광지로 취급하지 않습니다.

            방문 날짜 및 휴무일
            방문 날짜가 제공된 경우 DATASET의 closedDays만 확인합니다.
            방문 날짜와 closedDays가 충돌하는 장소는 추천하지 않습니다.

            루트 선정 우선순위
            장소를 선정하고 순서를 결정할 때 다음 요소를 종합적으로 고려합니다.
            1. 견종 및 반려견 컨디션
            2. 관광지·식당·카페 필수 구성
            3. 대형견의 야외석 조건
            4. 출발지 권역
            5. 보호자 성향
            6. 위경도를 이용한 장소 간 위치와 동선
            7. 방문 날짜와 휴무일 충돌 여부
            8. 전체 이동 동선의 자연스러움
            출발지에서 시작하여 도보 중심으로 자연스럽게 이동할 수 있도록 장소 순서를 결정합니다.
            최종적으로 주어진 모든 조건을 만족하는 가장 적합한 1일 추천 루트를 생성합니다.

            응답은 반드시 JSON 객체 하나만 반환합니다.
            형식: {"placeIds":[1,2,3]}
            placeIds에는 DATASET의 id만 넣고, 추천 방문 순서대로 정렬합니다.
            """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public OpenAiRecommendedPlaceSelector(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${openai.base-url:https://api.openai.com}") String baseUrl,
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.model:gpt-4.1-mini}") String model
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public List<Long> select(
            DepartureArea departureArea,
            LocalDate date,
            DogCondition condition,
            Pet representativePet,
            List<Place> places
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new GeneralException(RecommendedRouteErrorCode.AI_NOT_CONFIGURED);
        }

        OpenAiChatRequest request = new OpenAiChatRequest(
                model,
                List.of(
                        new Message("system", SYSTEM_PROMPT),
                        new Message("user", userPrompt(departureArea, date, condition, representativePet, places))
                ),
                new ResponseFormat("json_object")
        );

        try {
            OpenAiChatResponse response = restClient.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .body(OpenAiChatResponse.class);
            return parsePlaceIds(response);
        } catch (GeneralException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new GeneralException(RecommendedRouteErrorCode.AI_REQUEST_FAILED, exception);
        }
    }

    private String userPrompt(
            DepartureArea departureArea,
            LocalDate date,
            DogCondition condition,
            Pet pet,
            List<Place> places
    ) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "departureArea", departureArea.name(),
                    "date", date.toString(),
                    "dogCondition", condition.name(),
                    "dogSize", pet.getSize().name(),
                    "travelPreference", pet.getTravelPreference() == null ? "" : pet.getTravelPreference().name(),
                    "dataset", places.stream().map(this::toDatasetPlace).toList()
            ));
        } catch (JsonProcessingException exception) {
            throw new GeneralException(RecommendedRouteErrorCode.AI_REQUEST_FAILED, exception);
        }
    }

    private DatasetPlace toDatasetPlace(Place place) {
        return new DatasetPlace(
                place.getId(),
                place.getCategory().name(),
                place.getName(),
                place.getArea(),
                place.getDetailCategory(),
                place.getRoadAddress(),
                place.getClosedDays(),
                place.getLongitude(),
                place.getLatitude(),
                place.getPetAccessType(),
                place.getAllowedPets(),
                place.getPetRequirements(),
                place.getPetInfo()
        );
    }

    private List<Long> parsePlaceIds(OpenAiChatResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()
                || response.choices().get(0).message() == null) {
            throw new GeneralException(RecommendedRouteErrorCode.INVALID_AI_RESPONSE);
        }

        try {
            RecommendedPlaceIds result = objectMapper.readValue(
                    response.choices().get(0).message().content(),
                    RecommendedPlaceIds.class
            );
            if (result.placeIds() == null || result.placeIds().size() < 3 || result.placeIds().size() > 5
                    || Set.copyOf(result.placeIds()).size() != result.placeIds().size()) {
                throw new GeneralException(RecommendedRouteErrorCode.INVALID_AI_RESPONSE);
            }
            return result.placeIds();
        } catch (JsonProcessingException exception) {
            throw new GeneralException(RecommendedRouteErrorCode.INVALID_AI_RESPONSE, exception);
        }
    }

    private record OpenAiChatRequest(
            String model,
            List<Message> messages,
            @JsonProperty("response_format") ResponseFormat responseFormat
    ) {
    }

    private record Message(String role, String content) {
    }

    private record ResponseFormat(String type) {
    }

    private record OpenAiChatResponse(List<Choice> choices) {
    }

    private record Choice(Message message) {
    }

    private record RecommendedPlaceIds(List<Long> placeIds) {
    }

    private record DatasetPlace(
            Long id,
            String category,
            String name,
            String area,
            String detailCategory,
            String roadAddress,
            String closedDays,
            Double longitude,
            Double latitude,
            String petAccessType,
            String allowedPets,
            String petRequirements,
            String petInfo
    ) {
    }
}
