package com.example.gyeonjutravel.global.storage;

import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local")
public class LocalImageStorageService implements ImageStorageService {
    private final Path rootDirectory;

    public LocalImageStorageService(
            @Value("${app.storage.local.upload-dir:${java.io.tmpdir}/gyeonju-travel/images}") String uploadDirectory
    ) {
        this.rootDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    @Override
    public String upload(MultipartFile image, String directory) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        String filename = UUID.randomUUID() + ImageFileValidator.extensionOf(image.getContentType());
        Path targetDirectory = rootDirectory.resolve(directory).normalize();
        Path target = targetDirectory.resolve(filename).normalize();
        if (!target.startsWith(rootDirectory)) {
            throw new GeneralException(StorageErrorCode.IMAGE_UPLOAD_FAILED);
        }
        try {
            Files.createDirectories(targetDirectory);
            Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/api/" + directory + "/" + filename;
        } catch (IOException exception) {
            throw new GeneralException(StorageErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }
}
