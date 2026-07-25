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
                .andExpect(jsonPath("$.result.gender").value("FEMALE"));

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
}
