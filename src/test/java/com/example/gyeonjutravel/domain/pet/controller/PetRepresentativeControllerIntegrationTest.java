package com.example.gyeonjutravel.domain.pet.controller;

import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.member.repository.MemberRepository;
import com.example.gyeonjutravel.domain.pet.entity.Pet;
import com.example.gyeonjutravel.domain.pet.entity.enums.DogSize;
import com.example.gyeonjutravel.domain.pet.repository.PetRepository;
import com.example.gyeonjutravel.global.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PetRepresentativeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void memberCanChangeRepresentativePetBySelectingAnotherPet() throws Exception {
        Member member = saveMember("representative@example.com");
        Pet cherry = savePet(member, "앵두", true);
        Pet jjory = savePet(member, "쪼리", false);
        String accessToken = jwtTokenProvider.createAccessToken(member);

        mockMvc.perform(patch("/api/pets/{petId}/representative", jjory.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.representativePet.petId").value(jjory.getId()))
                .andExpect(jsonPath("$.result.representativePet.name").value("쪼리"))
                .andExpect(jsonPath("$.result.otherPets[0].petId").value(cherry.getId()))
                .andExpect(jsonPath("$.result.otherPets[0].name").value("앵두"));

        assertThat(petRepository.findFirstByMemberIdAndRepresentativeTrue(member.getId()))
                .get()
                .extracting(Pet::getId)
                .isEqualTo(jjory.getId());
        assertThat(petRepository.findAllByMemberIdAndRepresentativeFalseOrderByIdAsc(member.getId()))
                .extracting(Pet::getId)
                .containsExactly(cherry.getId());
    }

    @Test
    void memberCannotSelectAnotherMembersPetAsRepresentative() throws Exception {
        Member member = saveMember("owner@example.com");
        savePet(member, "앵두", true);
        Member anotherMember = saveMember("another@example.com");
        Pet anotherMembersPet = savePet(anotherMember, "쪼리", true);
        String accessToken = jwtTokenProvider.createAccessToken(member);

        mockMvc.perform(patch("/api/pets/{petId}/representative", anotherMembersPet.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PET_404_1"));
    }

    private Member saveMember(String email) {
        return memberRepository.save(Member.builder()
                .email(email)
                .password("encoded-password")
                .name("테스트 회원")
                .phoneNumber("010-0000-0000")
                .build());
    }

    private Pet savePet(Member member, String name, boolean representative) {
        return petRepository.save(Pet.builder()
                .member(member)
                .name(name)
                .size(DogSize.SMALL)
                .representative(representative)
                .build());
    }
}
