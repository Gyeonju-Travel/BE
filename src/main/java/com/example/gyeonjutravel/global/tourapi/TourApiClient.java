package com.example.gyeonjutravel.global.tourapi;

import com.example.gyeonjutravel.domain.place.exception.PlaceErrorCode;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TourApiClient {
    private final TourApiProperties properties;
    private final ObjectMapper mapper;
    private final RestClient client;
    private final Map<String, CachedResponse> responseCache = new ConcurrentHashMap<>();

    public TourApiClient(TourApiProperties properties, ObjectMapper mapper) {
        this.properties = properties;
        this.mapper = mapper;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());
        this.client = RestClient.builder().requestFactory(factory).build();
    }

    public List<JsonNode> attractions() {
        CachedResponse cached = getCached("attractions");
        if (cached != null) return cached.items();
        Map<String, JsonNode> result = new LinkedHashMap<>();
        int received = 0;
        for (int page = 1; page <= properties.getMaxPages(); page++) {
            JsonNode body = request("areaBasedList2", Map.of(
                    "contentTypeId", "12", "lDongRegnCd", properties.getLegalRegionCode(),
                    "lDongSignguCd", properties.getLegalDistrictCode(), "arrange", "A",
                    "numOfRows", properties.getPageSize(), "pageNo", page));
            List<JsonNode> items = items(body);
            received += items.size();
            for (JsonNode item : items) {
                if ("12".equals(item.path("contenttypeid").asText())) {
                    String id = item.path("contentid").asText();
                    if (!id.matches("[0-9]+")) throw unavailable();
                    result.put(id, item);
                }
            }
            if (received >= body.path("totalCount").asInt(-1) && body.has("totalCount")) {
                List<JsonNode> attractions = List.copyOf(result.values());
                putCached("attractions", attractions);
                return attractions;
            }
            if (items.isEmpty()) throw unavailable();
        }
        // 일부 페이지만 조회한 목록을 전체 조회 결과로 반환하지 않음.
        throw unavailable();
    }

    public JsonNode common(String contentId) {
        JsonNode item = single("detailCommon2", contentId, false);
        if (item == null || !"12".equals(item.path("contenttypeid").asText())) {
            throw new GeneralException(PlaceErrorCode.PLACE_NOT_FOUND);
        }
        return item;
    }

    public JsonNode intro(String contentId) { return single("detailIntro2", contentId, true); }
    public JsonNode pet(String contentId) { return single("detailPetTour2", contentId, false); }

    private JsonNode single(String operation, String contentId, boolean withType) {
        if (contentId == null || !contentId.matches("[0-9]+")) throw unavailable();
        String cacheKey = operation + ":" + contentId;
        CachedResponse cached = getCached(cacheKey);
        if (cached != null) return cached.item();
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("contentId", contentId);
        if (withType) params.put("contentTypeId", "12");
        List<JsonNode> items = items(request(operation, params));
        if (items.isEmpty()) {
            putCached(cacheKey, (JsonNode) null);
            return null;
        }
        JsonNode item = items.getFirst();
        if (!contentId.equals(item.path("contentid").asText())) throw unavailable();
        putCached(cacheKey, item);
        return item;
    }

    private CachedResponse getCached(String key) {
        if (properties.getCacheTtl() == null || properties.getCacheTtl().isZero()
                || properties.getCacheTtl().isNegative()) return null;
        CachedResponse cached = responseCache.get(key);
        if (cached == null || cached.expiresAtNanos() <= System.nanoTime()) {
            responseCache.remove(key, cached);
            return null;
        }
        return cached;
    }

    private void putCached(String key, List<JsonNode> items) {
        long ttlNanos = properties.getCacheTtl().toNanos();
        if (ttlNanos > 0) responseCache.put(key, new CachedResponse(items, null, System.nanoTime() + ttlNanos));
    }

    private void putCached(String key, JsonNode item) {
        long ttlNanos = properties.getCacheTtl().toNanos();
        if (ttlNanos > 0) responseCache.put(key, new CachedResponse(null, item, System.nanoTime() + ttlNanos));
    }

    private record CachedResponse(List<JsonNode> items, JsonNode item, long expiresAtNanos) {
    }

    private JsonNode request(String operation, Map<String, ?> params) {
        if (properties.getServiceKey() == null || properties.getServiceKey().isBlank()) {
            throw new GeneralException(PlaceErrorCode.TOUR_API_NOT_CONFIGURED);
        }
        long started = System.nanoTime();
        String requestId = java.util.UUID.randomUUID().toString();
        Integer httpStatus = null;
        String resultCode = "미확인";
        try {
            UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                    .pathSegment(operation).queryParam("serviceKey", "{serviceKey}")
                    .queryParam("MobileOS", "ETC").queryParam("MobileApp", "{mobileApp}")
                    .queryParam("_type", "json");
            params.forEach(uri::queryParam);
            // 인증키가 포함된 전체 URL 대신 조회 조건만 기록
            // log.info("[TourAPI] 호출 시작 requestId={} operation={} params={}", requestId, operation, params);
            var entity = client.get().uri(uri.encode().buildAndExpand(Map.of(
                    "serviceKey", properties.getServiceKey(), "mobileApp", properties.getMobileApp())).toUri())
                    .retrieve().toEntity(String.class);
            httpStatus = entity.getStatusCode().value();
            JsonNode response = mapper.readTree(entity.getBody()).path("response");
            String code = response.path("header").path("resultCode").asText();
            resultCode = code.matches("[0-9]{1,4}") ? code : "형식오류";
            if (!"0000".equals(code)) throw unavailable();
            JsonNode body = response.path("body");
            if (!body.isObject()) throw unavailable();
             /*log.info("[TourAPI] 응답 성공 requestId={} operation={} httpStatus={} resultCode={} itemCount={} totalCount={} elapsedMs={}",
                     requestId, operation, httpStatus, resultCode, items(body).size(), body.path("totalCount").asInt(-1),
                     (System.nanoTime() - started) / 1_000_000); */
            return body;
        } catch (Exception exception) {
            // HTTP 클라이언트의 예외에는 인증키가 포함된 URL이 담길 수 있음.
            if (exception instanceof org.springframework.web.client.RestClientResponseException httpException) {
                httpStatus = httpException.getStatusCode().value();
            }
             log.warn("[TourAPI] 호출 실패 requestId={} operation={} httpStatus={} resultCode={} errorType={} elapsedMs={}",
                     requestId, operation, httpStatus, resultCode, exception.getClass().getSimpleName(),
                     (System.nanoTime() - started) / 1_000_000);
            throw unavailable();
        }
    }

    private List<JsonNode> items(JsonNode body) {
        JsonNode item = body.path("items").path("item");
        if (item.isMissingNode() || item.isNull() || (item.isTextual() && item.asText().isBlank())) return List.of();
        if (item.isObject()) return List.of(item);
        if (!item.isArray()) throw unavailable();
        List<JsonNode> result = new ArrayList<>();
        item.forEach(result::add);
        return result;
    }

    private GeneralException unavailable() { return new GeneralException(PlaceErrorCode.TOUR_API_UNAVAILABLE); }
}
