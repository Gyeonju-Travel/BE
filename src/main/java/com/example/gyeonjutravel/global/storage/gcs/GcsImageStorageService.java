package com.example.gyeonjutravel.global.storage.gcs;

import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.example.gyeonjutravel.global.apiPayload.response.code.ErrorCode;
import com.example.gyeonjutravel.global.storage.ImageFileValidator;
import com.example.gyeonjutravel.global.storage.ImageStorageService;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "gcs")
public class GcsImageStorageService implements ImageStorageService {

    private final Storage storage;

    @Value("${app.storage.gcs.bucket}")
    private String bucket;

    @Value("${app.storage.gcs.public-base-url:https://storage.googleapis.com}")
    private String publicBaseUrl;

    @Override
    public String upload(MultipartFile image, String directory) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        if (directory == null || !directory.matches("[a-z0-9-]+")) {
            throw new IllegalArgumentException("GCS directory must contain lowercase letters, digits, or hyphens only.");
        }

        String objectName = directory + "/" + UUID.randomUUID() + ImageFileValidator.extensionOf(image.getContentType());
        BlobInfo blobInfo = BlobInfo.newBuilder(bucket, objectName)
                .setContentType(image.getContentType())
                .build();
        try {
            storage.create(blobInfo, image.getBytes());
            return publicBaseUrl + "/" + bucket + "/" + encodePath(objectName);
        } catch (IOException | RuntimeException exception) {
            throw new GeneralException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    private String encodePath(String objectName) {
        return URLEncoder.encode(objectName, StandardCharsets.UTF_8).replace("+", "%20").replace("%2F", "/");
    }
}
