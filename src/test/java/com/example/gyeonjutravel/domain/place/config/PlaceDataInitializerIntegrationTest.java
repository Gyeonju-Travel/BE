package com.example.gyeonjutravel.domain.place.config;

import com.example.gyeonjutravel.domain.member.entity.Gender;
import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.member.repository.MemberRepository;
import com.example.gyeonjutravel.domain.place.entity.PlaceCategory;
import com.example.gyeonjutravel.domain.place.repository.PlaceRepository;
import com.example.gyeonjutravel.global.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.place-data.initialize=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PlaceDataInitializerIntegrationTest {

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void allValidSpreadsheetRowsAreLoaded() {
        assertThat(placeRepository.count()).isEqualTo(113);
        assertThat(placeRepository.countByCategory(PlaceCategory.RESTAURANT)).isEqualTo(44);
        assertThat(placeRepository.countByCategory(PlaceCategory.CAFE)).isEqualTo(19);
        assertThat(placeRepository.countByCategory(PlaceCategory.ATTRACTION)).isEqualTo(50);
    }

    @Test
    @WithMockUser
    void placeApiReturnsFilteredPlaces() throws Exception {
        mockMvc.perform(get("/api/places").param("categories", "CAFE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.totalElements").value(19));
    }

    @Test
    void memberCanSaveListAndDeleteMultiplePlaceBookmarksWithRequestBody() throws Exception {
        Member member = memberRepository.save(Member.builder()
                .email("bookmark@example.com")
                .password("encoded-password")
                .name("북마크테스터")
                .birthDate(LocalDate.of(1995, 1, 1))
                .gender(Gender.FEMALE)
                .phoneNumber("010-1234-5678")
                .build());
        var places = placeRepository.findAll();
        Long firstPlaceId = places.stream()
                .filter(place -> place.getCategory() == PlaceCategory.RESTAURANT)
                .findFirst()
                .orElseThrow()
                .getId();
        Long secondPlaceId = places.stream()
                .filter(place -> place.getCategory() == PlaceCategory.CAFE)
                .findFirst()
                .orElseThrow()
                .getId();
        CustomUserDetails principal = new CustomUserDetails(member);

        mockMvc.perform(post("/api/places/{placeId}/bookmarks", firstPlaceId).with(user(principal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.id").value(firstPlaceId));

        mockMvc.perform(post("/api/places/{placeId}/bookmarks", secondPlaceId).with(user(principal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.id").value(secondPlaceId));

        mockMvc.perform(get("/api/places/bookmarks").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2));

        mockMvc.perform(get("/api/places/bookmarks")
                        .param("categories", "CAFE")
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1))
                .andExpect(jsonPath("$.result[0].id").value(secondPlaceId))
                .andExpect(jsonPath("$.result[0].category").value("CAFE"));

        mockMvc.perform(delete("/api/places/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placeIds\":[" + firstPlaceId + "," + secondPlaceId + "]}")
                        .with(user(principal)))
                .andExpect(status().isOk());

        assertThat(placeRepository.findBookmarkedPlacesByMemberId(member.getId())).isEmpty();
    }
}
