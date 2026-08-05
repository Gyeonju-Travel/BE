package com.example.gyeonjutravel.domain.mypage.controller;

import com.example.gyeonjutravel.domain.member.service.PasswordResetMailService;
import com.example.gyeonjutravel.domain.inquiry.repository.InquiryRepository;
import com.example.gyeonjutravel.domain.report.entity.enums.PetPolicy;
import com.example.gyeonjutravel.domain.report.repository.PlaceReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MypageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlaceReportRepository placeReportRepository;

    @Autowired
    private InquiryRepository inquiryRepository;

    @MockitoBean
    private PasswordResetMailService passwordResetMailService;

    private String accessToken;

    @BeforeEach
    void signUp() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "mypage@example.com",
                                  "password": "password123!",
                                  "passwordConfirmation": "password123!",
                                  "name": "경주",
                                  "birthDate": "1995-04-12",
                                  "gender": "FEMALE",
                                  "phoneNumber": "010-1234-5678"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        accessToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("result")
                .path("accessToken")
                .asText();
    }

    @Test
    void authenticatedMemberCanSubmitPlaceReportWithOptionalImage() throws Exception {
        MockMultipartFile request = new MockMultipartFile(
                "request",
                "request.json",
                MediaType.APPLICATION_JSON_VALUE,
                """
                        {
                          "placeName": "황리단길 반려견 카페",
                          "address": "경북 경주시 포석로 1000",
                          "petPolicies": ["PET_FRIENDLY", "LEASH_REQUIRED"],
                          "recommendationReason": "마당이 넓고 직원이 친절해요."
                        }
                        """.getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "place.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{1, 2, 3}
        );

        MvcResult result = mockMvc.perform(multipart("/api/place-reports")
                        .file(request)
                        .file(image)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.placeReportId").isNumber())
                .andExpect(jsonPath("$.result.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.result.imageUrl", startsWith("/api/place-reports/")))
                .andExpect(jsonPath("$.result.submittedAt").isNotEmpty())
                .andReturn();

        long reportId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("result")
                .path("placeReportId")
                .asLong();
        var saved = placeReportRepository.findById(reportId).orElseThrow();
        assertThat(saved.getMember().getEmail()).isEqualTo("mypage@example.com");
        assertThat(saved.getPlaceName()).isEqualTo("황리단길 반려견 카페");
        assertThat(saved.getPetPolicies()).isEqualTo(Set.of(PetPolicy.PET_FRIENDLY, PetPolicy.LEASH_REQUIRED));
    }

    @Test
    void authenticatedMemberCanSubmitInquiry() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/inquiries")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "일정 저장 문의",
                                  "content": "저장한 일정은 어디에서 확인할 수 있나요?"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.inquiryId").isNumber())
                .andExpect(jsonPath("$.result.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.result.submittedAt").isNotEmpty())
                .andReturn();

        long inquiryId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("result")
                .path("inquiryId")
                .asLong();
        var saved = inquiryRepository.findById(inquiryId).orElseThrow();
        assertThat(saved.getMember().getEmail()).isEqualTo("mypage@example.com");
        assertThat(saved.getTitle()).isEqualTo("일정 저장 문의");
    }

    @Test
    void inquiryRejectsBlankTitleAndContent() throws Exception {
        mockMvc.perform(post("/api/inquiries")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\" \",\"content\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400"))
                .andExpect(jsonPath("$.result.length()").value(2));
    }
}
