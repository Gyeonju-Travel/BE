package com.example.gyeonjutravel.global.storage.s3;

import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.example.gyeonjutravel.global.apiPayload.response.code.ErrorCode;
import com.example.gyeonjutravel.global.storage.ImageFileValidator;
import com.example.gyeonjutravel.global.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3", matchIfMissing = true)
public class S3ImageStorageService implements ImageStorageService {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${app.storage.s3.bucket}")
    private String bucket;

    @Value("${app.storage.s3.region}")
    private String region;

    @Override
    public String readUrl(String storedReference) {
        if (storedReference == null || storedReference.isBlank()) {
            return storedReference;
        }
        String key = storedReference;
        // Only migrate legacy URLs belonging to our configured bucket.
        if (storedReference.contains("://")) {
            URI uri = URI.create(storedReference);
            String host = uri.getHost();
            String regionalHost = bucket + ".s3." + region + ".amazonaws.com";
            if (!("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))
                    || !(regionalHost.equals(host) || (bucket + ".s3.amazonaws.com").equals(host))) {
                throw new IllegalArgumentException("Image URL does not belong to the configured S3 bucket.");
            }
            if (uri.getPath() == null || uri.getPath().length() <= 1) {
                throw new IllegalArgumentException("Image URL has no object key.");
            }
            key = uri.getPath().substring(1);
        }
        if (!(key.startsWith("pet-images/") || key.startsWith("stamp-albums/")
                || key.startsWith("place-reports/")) || key.contains("..")) {
            throw new IllegalArgumentException("Invalid stored image key.");
        }
        String objectKey = key;
        return s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(request -> request.bucket(bucket).key(objectKey))
                .build()).url().toString();
    }

    @Override
    public String upload(MultipartFile image, String directory) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        if (directory == null || !directory.matches("[a-z0-9-]+")) {
            throw new IllegalArgumentException("S3 directory must contain lowercase letters, digits, or hyphens only.");
        }

        String key = directory + "/" + UUID.randomUUID() + ImageFileValidator.extensionOf(image.getContentType());
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(image.getContentType())
                .build();
        try {
            s3Client.putObject(request, RequestBody.fromInputStream(image.getInputStream(), image.getSize()));
            return key;
        } catch (IOException | RuntimeException exception) {
            throw new GeneralException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }
}
