package com.example.gyeonjutravel.domain.pet.service;

import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.member.exception.MemberErrorCode;
import com.example.gyeonjutravel.domain.member.repository.MemberRepository;
import com.example.gyeonjutravel.domain.pet.dto.request.PetOnboardingRequest;
import com.example.gyeonjutravel.domain.pet.dto.request.PetProfileUpdateRequest;
import com.example.gyeonjutravel.domain.pet.dto.request.PetRegistrationRequest;
import com.example.gyeonjutravel.domain.pet.dto.response.PetDetailResponse;
import com.example.gyeonjutravel.domain.pet.dto.response.PetListResponse;
import com.example.gyeonjutravel.domain.pet.dto.response.PetSummaryResponse;
import com.example.gyeonjutravel.domain.pet.dto.response.PetOnboardingResponse;
import com.example.gyeonjutravel.domain.pet.dto.response.RepresentativePetResponse;
import com.example.gyeonjutravel.domain.pet.entity.Pet;
import com.example.gyeonjutravel.domain.pet.exception.PetErrorCode;
import com.example.gyeonjutravel.domain.pet.repository.PetRepository;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {

    private final PetRepository petRepository;
    private final MemberRepository memberRepository;
    private final PetImageStorageService petImageStorageService;

    @Transactional
    public PetOnboardingResponse completeOnboarding(
            Long memberId,
            PetOnboardingRequest request,
            MultipartFile image
    ) {
        if (petRepository.existsByMemberIdAndRepresentativeTrue(memberId)) {
            throw new GeneralException(PetErrorCode.ONBOARDING_ALREADY_COMPLETED);
        }

        Pet pet = petRepository.save(Pet.builder()
                .member(findMember(memberId))
                .name(request.name().trim())
                .profileImageUrl(petImageStorageService.store(image))
                .size(request.size())
                .representative(true)
                .travelPreference(request.travelPreference())
                .walkingStyle(request.walkingStyle())
                .build());
        return PetOnboardingResponse.from(pet);
    }

    @Transactional
    public PetDetailResponse register(Long memberId, PetRegistrationRequest request, MultipartFile image) {
        Pet pet = petRepository.save(Pet.builder()
                .member(findMember(memberId))
                .name(request.name().trim())
                .profileImageUrl(petImageStorageService.store(image))
                .breed(request.breed().trim())
                .size(request.size())
                .age(request.age())
                .gender(request.gender())
                .personality(request.personality())
                .representative(false)
                .build());
        return PetDetailResponse.from(pet);
    }

    public PetListResponse getMyPets(Long memberId) {
        RepresentativePetResponse representativePet = petRepository
                .findFirstByMemberIdAndRepresentativeTrue(memberId)
                .map(RepresentativePetResponse::from)
                .orElse(null);
        List<PetSummaryResponse> otherPets = petRepository
                .findAllByMemberIdAndRepresentativeFalseOrderByIdAsc(memberId)
                .stream()
                .map(PetSummaryResponse::from)
                .toList();
        return new PetListResponse(representativePet, otherPets);
    }

    public PetDetailResponse get(Long memberId, Long petId) {
        return PetDetailResponse.from(findOwnedPet(memberId, petId));
    }

    @Transactional
    public PetListResponse changeRepresentative(Long memberId, Long petId) {
        List<Pet> pets = petRepository.findAllByMemberIdForUpdate(memberId);
        Pet selectedPet = pets.stream()
                .filter(pet -> pet.getId().equals(petId))
                .findFirst()
                .orElseThrow(() -> new GeneralException(PetErrorCode.PET_NOT_FOUND));

        pets.forEach(pet -> {
            if (pet == selectedPet) {
                pet.markAsRepresentative();
            } else {
                pet.unmarkAsRepresentative();
            }
        });

        List<PetSummaryResponse> otherPets = pets.stream()
                .filter(pet -> pet != selectedPet)
                .map(PetSummaryResponse::from)
                .toList();
        return new PetListResponse(RepresentativePetResponse.from(selectedPet), otherPets);
    }

    @Transactional
    public PetDetailResponse updateProfile(
            Long memberId,
            Long petId,
            PetProfileUpdateRequest request,
            MultipartFile image
    ) {
        Pet pet = findOwnedPet(memberId, petId);
        String profileImageUrl = image == null || image.isEmpty()
                ? pet.getProfileImageUrl()
                : petImageStorageService.store(image);
        pet.updateProfile(
                request.name().trim(),
                profileImageUrl,
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

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

}
