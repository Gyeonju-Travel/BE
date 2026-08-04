package com.example.gyeonjutravel.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.gyeonjutravel.domain.member.service.PasswordResetMailService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PasswordResetMailService passwordResetMailService;

    @Test
    void memberCanSignUpAndResetPasswordWithEmailVerificationCode() throws Exception {
        String email = "member@example.com";

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "member@example.com",
                                  "password": "password123!",
                                  "passwordConfirmation": "password123!",
                                  "name": "김견주",
                                  "birthDate": "1995-04-12",
                                  "gender": "FEMALE",
                                  "phoneNumber": "010-1234-5678"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.email").value(email))
                .andExpect(jsonPath("$.result.name").value("김견주"))
                .andExpect(jsonPath("$.result.birthDate").value("1995-04-12"))
                .andExpect(jsonPath("$.result.gender").value("FEMALE"))
                .andExpect(jsonPath("$.result.onboardingCompleted").value(false));

        mockMvc.perform(post("/api/auth/password-reset/verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"member@example.com\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(passwordResetMailService).sendVerificationCode(eq(email), codeCaptor.capture(), eq(5L));
        String verificationCode = codeCaptor.getValue();
        assertThat(verificationCode).matches("\\d{6}");

        MvcResult verificationResult = mockMvc.perform(
                        post("/api/auth/password-reset/verification-code/confirm")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "member@example.com",
                                          "verificationCode": "%s"
                                        }
                                        """.formatted(verificationCode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.resetToken").isNotEmpty())
                .andExpect(jsonPath("$.result.expiresIn").value(600))
                .andReturn();
        String resetToken = objectMapper.readTree(verificationResult.getResponse().getContentAsString())
                .path("result")
                .path("resetToken")
                .asText();

        mockMvc.perform(patch("/api/auth/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "member@example.com",
                                  "resetToken": "%s",
                                  "newPassword": "newPassword123!",
                                  "newPasswordConfirmation": "newPassword123!"
                                }
                                """.formatted(resetToken)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "member@example.com",
                                  "password": "newPassword123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.accessToken").isNotEmpty());
    }

    @Test
    void signUpRejectsMismatchedPasswordConfirmation() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "mismatch@example.com",
                                  "password": "password123!",
                                  "passwordConfirmation": "different123!",
                                  "name": "김견주",
                                  "birthDate": "1995-04-12",
                                  "gender": "MALE",
                                  "phoneNumber": "010-1234-5678"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MEMBER_400_1"));
    }

    @Test
    void memberCanCreateRetrieveAndUpdatePet() throws Exception {
        MvcResult signUpResult = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "onboarding@example.com",
                                  "password": "password123!",
                                  "passwordConfirmation": "password123!",
                                  "name": "김견주",
                                  "birthDate": "1995-04-12",
                                  "gender": "FEMALE",
                                  "phoneNumber": "010-1234-5678"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken = objectMapper.readTree(signUpResult.getResponse().getContentAsString())
                .path("result")
                .path("accessToken")
                .asText();

        MvcResult petCreateResult = mockMvc.perform(multipart("/api/onboarding")
                        .file(jsonPart("""
                                {
                                  "name": "황남이",
                                  "travelPreference": "PHOTO_SPOT",
                                  "size": "SMALL",
                                  "personality": "RELAXED"
                                }
                                """))
                        .file(imagePart("hwangnam.png"))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.name").value("황남이"))
                .andExpect(jsonPath("$.result.profileImageUrl").value(startsWith("/api/pet-images/")))
                .andExpect(jsonPath("$.result.travelPreference").value("PHOTO_SPOT"))
                .andExpect(jsonPath("$.result.size").value("SMALL"))
                .andExpect(jsonPath("$.result.personality").value("RELAXED"))
                .andExpect(jsonPath("$.result.breed").doesNotExist())
                .andExpect(jsonPath("$.result.age").doesNotExist())
                .andExpect(jsonPath("$.result.gender").doesNotExist())
                .andExpect(jsonPath("$.result.walkingStyle").doesNotExist())
                .andReturn();
        long petId = objectMapper.readTree(petCreateResult.getResponse().getContentAsString())
                .path("result")
                .path("petId")
                .asLong();

        mockMvc.perform(get("/api/pets/{petId}", petId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.personality").value("RELAXED"));

        mockMvc.perform(multipart("/api/pets/{petId}", petId)
                        .file(jsonPart("""
                                {
                                  "name": "황남이",
                                  "breed": "웰시코기",
                                  "size": "MEDIUM",
                                  "age": 4,
                                  "gender": "MALE",
                                  "personality": "ACTIVE"
                                }
                                """))
                        .file(imagePart("hwangnam-new.png"))
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.breed").value("웰시코기"))
                .andExpect(jsonPath("$.result.size").value("MEDIUM"))
                .andExpect(jsonPath("$.result.age").value(4))
                .andExpect(jsonPath("$.result.gender").value("MALE"))
                .andExpect(jsonPath("$.result.personality").value("ACTIVE"))
                .andExpect(jsonPath("$.result.travelPreference").doesNotExist())
                .andExpect(jsonPath("$.result.walkingStyle").doesNotExist());

        mockMvc.perform(get("/api/pets/{petId}", petId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.petId").value(petId))
                .andExpect(jsonPath("$.result.breed").value("웰시코기"))
                .andExpect(jsonPath("$.result.age").value(4))
                .andExpect(jsonPath("$.result.travelPreference").doesNotExist())
                .andExpect(jsonPath("$.result.walkingStyle").doesNotExist());

        MvcResult secondPetCreateResult = mockMvc.perform(multipart("/api/pets")
                        .file(jsonPart("""
                                {
                                  "name": "첨성대",
                                  "breed": "골든리트리버",
                                  "size": "LARGE",
                                  "age": 2,
                                  "gender": "FEMALE",
                                  "personality": "FRIENDLY"
                                }
                                """))
                        .file(imagePart("cheomseongdae.png"))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.name").value("첨성대"))
                .andExpect(jsonPath("$.result.breed").value("골든리트리버"))
                .andExpect(jsonPath("$.result.size").value("LARGE"))
                .andExpect(jsonPath("$.result.age").value(2))
                .andExpect(jsonPath("$.result.gender").value("FEMALE"))
                .andExpect(jsonPath("$.result.personality").value("FRIENDLY"))
                .andExpect(jsonPath("$.result.travelPreference").doesNotExist())
                .andExpect(jsonPath("$.result.walkingStyle").doesNotExist())
                .andReturn();
        long secondPetId = objectMapper.readTree(secondPetCreateResult.getResponse().getContentAsString())
                .path("result")
                .path("petId")
                .asLong();

        mockMvc.perform(get("/api/pets")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.representativePet.petId").value(petId))
                .andExpect(jsonPath("$.result.representativePet.name").value("황남이"))
                .andExpect(jsonPath("$.result.representativePet.profileImageUrl")
                        .value(startsWith("/api/pet-images/")))
                .andExpect(jsonPath("$.result.representativePet.breed").value("웰시코기"))
                .andExpect(jsonPath("$.result.representativePet.size").value("MEDIUM"))
                .andExpect(jsonPath("$.result.representativePet.age").value(4))
                .andExpect(jsonPath("$.result.representativePet.gender").doesNotExist())
                .andExpect(jsonPath("$.result.representativePet.personality").doesNotExist())
                .andExpect(jsonPath("$.result.representativePet.travelPreference").doesNotExist())
                .andExpect(jsonPath("$.result.representativePet.walkingStyle").doesNotExist())
                .andExpect(jsonPath("$.result.otherPets[0].petId").value(secondPetId))
                .andExpect(jsonPath("$.result.otherPets[0].name").value("첨성대"))
                .andExpect(jsonPath("$.result.otherPets[0].profileImageUrl")
                        .value(startsWith("/api/pet-images/")))
                .andExpect(jsonPath("$.result.otherPets[0].size").doesNotExist());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "onboarding@example.com",
                                  "password": "password123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.onboardingCompleted").value(true));
    }

    @Test
    void petCreationRequiresAuthenticationAndRequiredSelections() throws Exception {
        mockMvc.perform(multipart("/api/pets"))
                .andExpect(status().isUnauthorized());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "validation@example.com",
                                  "password": "password123!",
                                  "passwordConfirmation": "password123!",
                                  "name": "김견주",
                                  "birthDate": "1995-04-12",
                                  "gender": "MALE",
                                  "phoneNumber": "010-9876-5432"
                                }
                                """))
                .andReturn();
        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("result")
                .path("accessToken")
                .asText();

        mockMvc.perform(multipart("/api/pets")
                        .file(jsonPart("{\"name\":\"황남이\"}"))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").isArray());
    }

    private MockMultipartFile jsonPart(String json) {
        return new MockMultipartFile(
                "request",
                "request.json",
                MediaType.APPLICATION_JSON_VALUE,
                json.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }

    private MockMultipartFile imagePart(String filename) {
        return new MockMultipartFile(
                "image",
                filename,
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}
        );
    }
}
