package com.example.gyeonjutravel.global.storage;

import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import java.util.Map;

final class ImageFileValidator {
    private static final Map<String, String> ALLOWED_EXTENSIONS = Map.of(
            "image/jpeg", ".jpg", "image/png", ".png", "image/webp", ".webp"
    );

    private ImageFileValidator() {
    }

    static String extensionOf(String contentType) {
        String extension = ALLOWED_EXTENSIONS.get(contentType);
        if (extension == null) {
            throw new GeneralException(StorageErrorCode.INVALID_IMAGE);
        }
        return extension;
    }
}
