package com.example.gyeonjutravel.domain.pet.service;

import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.member.exception.MemberErrorCode;
import com.example.gyeonjutravel.domain.member.repository.MemberRepository;
import com.example.gyeonjutravel.domain.pet.dto.request.PetCreateRequest;
import com.example.gyeonjutravel.domain.pet.dto.request.PetProfileUpdateRequest;
import com.example.gyeonjutravel.domain.pet.dto.response.PetCreateResponse;
import com.example.gyeonjutravel.domain.pet.dto.response.PetDetailResponse;
import com.example.gyeonjutravel.domain.pet.entity.Pet;
import com.example.gyeonjutravel.domain.pet.exception.PetErrorCode;
import com.example.gyeonjutravel.domain.pet.repository.PetRepository;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {

    private final PetRepository petRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public PetCreateResponse create(Long memberId, PetCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));

        Pet pet = petRepository.save(Pet.builder()
                .member(member)
                .name(request.name().trim())
                .profileImageUrl(normalizeNullableText(request.profileImageUrl()))
                .size(request.size())
                .travelPreference(request.travelPreference())
                .walkingStyle(request.walkingStyle())
                .build());
        return PetCreateResponse.from(pet);
    }

    public List<PetDetailResponse> getMyPets(Long memberId) {
        return petRepository.findAllByMemberIdOrderByIdAsc(memberId).stream()
                .map(PetDetailResponse::from)
                .toList();
    }

    public PetDetailResponse get(Long memberId, Long petId) {
        return PetDetailResponse.from(findOwnedPet(memberId, petId));
    }

    @Transactional
    public PetDetailResponse updateProfile(Long memberId, Long petId, PetProfileUpdateRequest request) {
        Pet pet = findOwnedPet(memberId, petId);
        pet.updateProfile(
                request.name().trim(),
                normalizeNullableText(request.profileImageUrl()),
                request.breed().trim(),
                request.size(),
                request.age(),
                request.gender(),
                request.personality()
        );
        return PetDetailResponse.from(pet);
    }

    private Pet findOwnedPet(Long memberId, Long petId) {
        return petRepository.findByIdAndMemberId(petId, memberId)
                .orElseThrow(() -> new GeneralException(PetErrorCode.PET_NOT_FOUND));
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
