package com.example.gyeonjutravel.global.storage.s3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

class S3ImageStorageServiceTest {
    private S3Presigner presigner;
    private S3ImageStorageService storage;

    @BeforeEach
    void setUp() {
        presigner = S3Presigner.builder().region(Region.AP_NORTHEAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test-access", "test-secret"))).build();
        storage = new S3ImageStorageService(mock(S3Client.class), presigner);
        ReflectionTestUtils.setField(storage, "bucket", "test-images");
        ReflectionTestUtils.setField(storage, "region", "ap-northeast-2");
    }

    @AfterEach
    void close() {
        presigner.close();
    }

    @Test
    void uploadStoresKeyRatherThanExpiringUrl() {
        String key = storage.upload(new MockMultipartFile("image", "photo.png", "image/png", new byte[]{1}), "pet-images");
        assertThat(key).startsWith("pet-images/").endsWith(".png").doesNotContain("?", "https:");
    }

    @Test
    void signsNewKeysAndLegacyUrlsWithTenMinuteExpiry() {
        for (String reference : new String[]{"pet-images/photo.png",
                "https://test-images.s3.ap-northeast-2.amazonaws.com/pet-images/photo.png",
                "https://test-images.s3.amazonaws.com/pet-images/photo.png"}) {
            assertThat(storage.readUrl(reference)).contains("/pet-images/photo.png", "X-Amz-Signature=", "X-Amz-Expires=600");
        }
        assertThat(storage.readUrl("stamp-albums/photo.png")).contains("X-Amz-Signature=");
    }

    @Test
    void rejectsOtherBucketsAndUnrelatedKeys() {
        assertThatIllegalArgumentException().isThrownBy(() -> storage.readUrl("https://other.s3.amazonaws.com/pet-images/photo.png"));
        assertThatIllegalArgumentException().isThrownBy(() -> storage.readUrl("private/secret.json"));
        assertThatIllegalArgumentException().isThrownBy(() -> storage.readUrl("pet-images/../secret.json"));
        assertThat(storage.readUrl(null)).isNull();
    }
}
