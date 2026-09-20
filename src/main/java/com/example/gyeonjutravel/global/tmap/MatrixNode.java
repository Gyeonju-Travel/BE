package com.example.gyeonjutravel.global.tmap;

public record MatrixNode(String key, double longitude, double latitude, String tourContentId) {
    public MatrixNode(String key, double longitude, double latitude) {
        this(key, longitude, latitude, null);
    }
}
