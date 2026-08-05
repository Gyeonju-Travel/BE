package com.example.gyeonjutravel.global.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    String upload(MultipartFile image, String directory);
}
