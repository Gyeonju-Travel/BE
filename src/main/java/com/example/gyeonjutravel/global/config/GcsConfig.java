package com.example.gyeonjutravel.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Configuration
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "gcs")
public class GcsConfig {

    @Bean
    public Storage googleCloudStorage(
            @Value("${GOOGLE_APPLICATION_CREDENTIALS:}") String credentialsPath
    ) throws IOException {
        if (credentialsPath == null || credentialsPath.isBlank()) {
            return StorageOptions.getDefaultInstance().getService();
        }

        Path path = resolveCredentialsPath(credentialsPath)
                .orElseGet(() -> Path.of(stripQuotes(credentialsPath)).toAbsolutePath().normalize());
        try (InputStream credentials = Files.newInputStream(path)) {
            return StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(credentials))
                    .build()
                    .getService();
        }
    }

    private Optional<Path> resolveCredentialsPath(String credentialsPath) {
        Optional<Path> configuredPath = toExistingPath(credentialsPath);
        if (configuredPath.isPresent()) {
            return configuredPath;
        }
        return readRawCredentialsPathFromEnvFile().flatMap(this::toExistingPath);
    }

    private Optional<Path> toExistingPath(String value) {
        try {
            Path path = Path.of(stripQuotes(value)).toAbsolutePath().normalize();
            return Files.exists(path) ? Optional.of(path) : Optional.empty();
        } catch (InvalidPathException exception) {
            return Optional.empty();
        }
    }

    private Optional<String> readRawCredentialsPathFromEnvFile() {
        Path envFile = Path.of("env").toAbsolutePath().normalize();
        if (!Files.exists(envFile)) {
            return Optional.empty();
        }
        try {
            return Files.readAllLines(envFile).stream()
                    .filter(line -> line.startsWith("GOOGLE_APPLICATION_CREDENTIALS="))
                    .map(line -> line.substring("GOOGLE_APPLICATION_CREDENTIALS=".length()))
                    .findFirst();
        } catch (IOException exception) {
            return Optional.empty();
        }
    }

    private String stripQuotes(String value) {
        String trimmed = value.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }
}
