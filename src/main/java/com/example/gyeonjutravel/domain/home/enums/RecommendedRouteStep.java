package com.example.gyeonjutravel.domain.home.enums;

public enum RecommendedRouteStep {
    DEPARTURE_ANALYZING("출발지 분석중"),
    COURSE_SEARCHING("코스 탐색중"),
    CONDITION_CHECKING("컨디션 체크중"),
    ROUTE_COMPLETED("추천 경로 설정 완료");

    private final String message;

    RecommendedRouteStep(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
