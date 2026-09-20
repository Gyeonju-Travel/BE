package com.example.gyeonjutravel.global.tourapi;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "tour-api")
public class TourApiProperties {
    private String baseUrl = "https://apis.data.go.kr/B551011/KorPetTourService2";
    // TOUR_API_SERVICE_KEY에는 디코딩된 인증키를 설정하며, 요청 URL은 로그에 남기지 않음.
    private String serviceKey = "";
    private String mobileApp = "GyeonjuTravel";
    private String legalRegionCode = "47";
    private String legalDistrictCode = "130";
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(7);
    private int pageSize = 100;
    private int maxPages = 10;
}
