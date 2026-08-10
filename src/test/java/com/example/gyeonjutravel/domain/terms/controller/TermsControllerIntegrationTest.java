package com.example.gyeonjutravel.domain.terms.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TermsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anyoneCanGetSignUpTerms() throws Exception {
        mockMvc.perform(get("/api/terms/signup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.terms.length()").value(4))
                .andExpect(jsonPath("$.result.terms[0].code").value("TERMS_OF_SERVICE"))
                .andExpect(jsonPath("$.result.terms[0].required").value(true));
    }

    @Test
    void anyoneCanAgreeSignUpTermsAndReceiveToken() throws Exception {
        mockMvc.perform(post("/api/terms/signup/agreement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "termsOfServiceAgreed": true,
                                  "privacyPolicyAgreed": true,
                                  "locationServiceAgreed": true,
                                  "ageOverFourteenAgreed": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.agreementToken").isNotEmpty());
    }
}
