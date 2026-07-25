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
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

        MvcResult petCreateResult = mockMvc.perform(post("/api/pets")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "황남이",
                                  "profileImageUrl": "https://cdn.example.com/dogs/hwangnam.png",
                                  "travelPreference": "PHOTO_SPOT",
                                  "size": "SMALL",
                                  "walkingStyle": "SHORT_WALK"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.name").value("황남이"))
                .andExpect(jsonPath("$.result.travelPreference").value("PHOTO_SPOT"))
                .andExpect(jsonPath("$.result.size").value("SMALL"))
                .andExpect(jsonPath("$.result.walkingStyle").value("SHORT_WALK"))
                .andExpect(jsonPath("$.result.breed").doesNotExist())
                .andExpect(jsonPath("$.result.age").doesNotExist())
                .andExpect(jsonPath("$.result.gender").doesNotExist())
                .andExpect(jsonPath("$.result.personality").doesNotExist())
                .andReturn();
        long petId = objectMapper.readTree(petCreateResult.getResponse().getContentAsString())
                .path("result")
                .path("petId")
                .asLong();

        mockMvc.perform(get("/api/pets")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].petId").value(petId))
                .andExpect(jsonPath("$.result[0].name").value("황남이"))
                .andExpect(jsonPath("$.result[0].profileImageUrl")
                        .value("https://cdn.example.com/dogs/hwangnam.png"))
                .andExpect(jsonPath("$.result[0].breed").doesNotExist())
                .andExpect(jsonPath("$.result[0].travelPreference").doesNotExist())
                .andExpect(jsonPath("$.result[0].walkingStyle").doesNotExist());

        mockMvc.perform(patch("/api/pets/{petId}", petId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "황남이",
                                  "profileImageUrl": "https://cdn.example.com/dogs/hwangnam-new.png",
                                  "breed": "웰시코기",
                                  "size": "MEDIUM",
                                  "age": 4,
                                  "gender": "MALE",
                                  "personality": "ACTIVE"
                                }
                                """))
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
        mockMvc.perform(post("/api/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
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

        mockMvc.perform(post("/api/pets")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"황남이\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").isArray());
    }
}
