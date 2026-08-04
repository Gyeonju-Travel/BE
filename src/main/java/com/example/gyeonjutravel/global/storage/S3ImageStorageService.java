package com.example.gyeonjutravel.global.storage;

import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.example.gyeonjutravel.global.apiPayload.response.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3", matchIfMissing = true)
public class S3ImageStorageService implements ImageStorageService {
    private final S3Client s3Client;

    @Value("${app.storage.s3.bucket}")
    private String bucket;

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
            return s3Client.utilities().getUrl(GetUrlRequest.builder().bucket(bucket).key(key).build()).toString();
        } catch (IOException | RuntimeException exception) {
            throw new GeneralException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }
}
