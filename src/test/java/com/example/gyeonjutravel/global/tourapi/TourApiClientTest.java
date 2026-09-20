package com.example.gyeonjutravel.global.tourapi;

import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

class TourApiClientTest {
    private HttpServer server;
    private TourApiProperties properties;
    private TourApiClient client;
    private final List<String> requests = new ArrayList<>();
    private final AtomicReference<String> response = new AtomicReference<>();

    @BeforeEach
    void setup() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requests.add(exchange.getRequestURI().toASCIIString());
            byte[] bytes = response.get().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (var output = exchange.getResponseBody()) { output.write(bytes); }
        });
        server.start();
        properties = new TourApiProperties();
        properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
        properties.setServiceKey("test+key/with=characters");
        properties.setReadTimeout(Duration.ofMillis(200));
        client = new TourApiClient(properties, new ObjectMapper());
    }

    @AfterEach
    void stop() { server.stop(0); }

    @Test
    void listCallsUpstreamEveryTimeWithAttractionAndGyeongjuFiltersAndEncodesKeyOnce() {
        response.set(body("{\"contentid\":\"123\",\"contenttypeid\":\"12\"}", 1));
        assertThat(client.attractions()).hasSize(1);
        assertThat(client.attractions()).hasSize(1);
        assertThat(requests).hasSize(2).allSatisfy(uri -> assertThat(uri)
                .contains("/areaBasedList2?", "contentTypeId=12", "areaCode=35", "sigunguCode=2",
                        "serviceKey=test%2Bkey%2Fwith%3Dcharacters", "_type=json"));
    }

    @Test
    void readsEveryPageAndDoesNotIncludeFood() {
        properties.setPageSize(1);
        response.set(body("[{\"contentid\":\"123\",\"contenttypeid\":\"12\"},"
                + "{\"contentid\":\"456\",\"contenttypeid\":\"39\"}]", 4));
        assertThat(client.attractions()).hasSize(1);
        assertThat(requests).hasSize(2);
        assertThat(requests.get(1)).contains("pageNo=2");
    }

    @Test
    void emptyItemsAreValidForAnEmptyCatalogue() {
        response.set(body("\"\"", 0));
        assertThat(client.attractions()).isEmpty();
    }

    @Test
    void emptyOptionalPetDetailIsNotAnAssumedPermission() {
        response.set(body("\"\"", 0));
        assertThat(client.pet("123")).isNull();
    }

    @Test
    void detailUsesContentIdAndIntroAlsoUsesType() {
        response.set(body("{\"contentid\":\"123\",\"contenttypeid\":\"12\"}", 1));
        client.common("123"); client.intro("123"); client.pet("123");
        assertThat(requests.get(0)).contains("/detailCommon2?", "contentId=123");
        assertThat(requests.get(1)).contains("/detailIntro2?", "contentTypeId=12");
        assertThat(requests.get(2)).contains("/detailPetTour2?", "contentId=123");
    }

    @Test
    void upstreamErrorAndMalformedXmlNeverExposeTheServiceKey() {
        for (String payload : List.of("{\"response\":{\"header\":{\"resultCode\":\"22\"}}}",
                "<OpenAPI_ServiceResponse>denied</OpenAPI_ServiceResponse>", "null")) {
            response.set(payload);
            assertThatThrownBy(client::attractions).isInstanceOf(GeneralException.class)
                    .hasMessageNotContaining(properties.getServiceKey()).hasNoCause();
        }
    }

    @Test
    void missingKeyMakesNoNetworkCall() {
        properties.setServiceKey("");
        assertThatThrownBy(client::attractions).isInstanceOf(GeneralException.class);
        assertThat(requests).isEmpty();
    }

    @Test
    void refusesPartialCatalogueWhenPageLimitIsExceeded() {
        properties.setMaxPages(1);
        response.set(body("{\"contentid\":\"123\",\"contenttypeid\":\"12\"}", 2));
        assertThatThrownBy(client::attractions).isInstanceOf(GeneralException.class);
    }

    @Test
    void rejectsWrongContentIdInDetail() {
        response.set(body("{\"contentid\":\"999\",\"contenttypeid\":\"12\"}", 1));
        assertThatThrownBy(() -> client.common("123")).isInstanceOf(GeneralException.class);
    }

    private static String body(String items, int total) {
        return "{\"response\":{\"header\":{\"resultCode\":\"0000\"},\"body\":{\"totalCount\":"
                + total + ",\"items\":{\"item\":" + items + "}}}}";
    }
}
