package com.example.gyeonjutravel.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp(
            @Value("${app.firebase.credentials-path:}") String credentialsPath
    ) throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }
        if (credentialsPath == null || credentialsPath.isBlank()) {
            return FirebaseApp.initializeApp();
        }

        Path path = resolveCredentialsPath(credentialsPath)
                .orElseGet(() -> Path.of(stripQuotes(credentialsPath)).toAbsolutePath().normalize());
        try (InputStream credentials = Files.newInputStream(path)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentials))
                    .build();
            return FirebaseApp.initializeApp(options);
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
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
                    .filter(line -> line.startsWith("FIREBASE_ADMIN_SDK_PATH="))
                    .map(line -> line.substring("FIREBASE_ADMIN_SDK_PATH=".length()))
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
