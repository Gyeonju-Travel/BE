package com.example.gyeonjutravel.domain.pet.service;

import com.example.gyeonjutravel.domain.pet.exception.PetErrorCode;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class PetImageStorageService {

    private static final String IMAGE_URL_PREFIX = "/api/pet-images/";
    private static final Map<String, String> ALLOWED_IMAGE_EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final Path uploadDirectory;

    public PetImageStorageService(
            @Value("${app.pet-image.upload-dir:${java.io.tmpdir}/gyeonju-travel/pet-images}") String uploadDirectory
    ) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    public String store(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
        }

        String extension = ALLOWED_IMAGE_EXTENSIONS.get(image.getContentType());
        if (extension == null) {
            throw new GeneralException(PetErrorCode.INVALID_PET_IMAGE);
        }

        String storedFilename = UUID.randomUUID() + extension;
        Path target = uploadDirectory.resolve(storedFilename).normalize();
        try {
            Files.createDirectories(uploadDirectory);
            Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return IMAGE_URL_PREFIX + storedFilename;
        } catch (IOException exception) {
            throw new GeneralException(PetErrorCode.PET_IMAGE_STORAGE_FAILED);
        }
    }

    public Resource load(String filename) {
        if (!filename.matches("[0-9a-fA-F-]+\\.(jpg|png|webp)")) {
            throw new GeneralException(PetErrorCode.PET_IMAGE_NOT_FOUND);
        }

        Path imagePath = uploadDirectory.resolve(filename).normalize();
        if (!imagePath.startsWith(uploadDirectory) || !Files.isRegularFile(imagePath)) {
            throw new GeneralException(PetErrorCode.PET_IMAGE_NOT_FOUND);
        }

        try {
            return new UrlResource(imagePath.toUri());
        } catch (IOException exception) {
            throw new GeneralException(PetErrorCode.PET_IMAGE_NOT_FOUND);
        }
    }
}
