package com.example.gyeonjutravel.domain.pet.service;

import com.example.gyeonjutravel.domain.member.repository.MemberRepository;
import com.example.gyeonjutravel.domain.pet.entity.Pet;
import com.example.gyeonjutravel.domain.pet.entity.enums.PetPersonality;
import com.example.gyeonjutravel.domain.pet.repository.PetRepository;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.example.gyeonjutravel.global.storage.ImageStorageService;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class PrivatePetImageTest {
    private final PetRepository pets = mock(PetRepository.class);
    private final ImageStorageService images = mock(ImageStorageService.class);
    private final PetService service = new PetService(pets, mock(MemberRepository.class), images);

    @Test
    void ownerGetsSignedUrlWithoutChangingStoredReference() {
        Pet pet = Pet.builder().name("Test").profileImageUrl("pet-images/photo.png")
                .personality(PetPersonality.ACTIVE).secondPersonality(PetPersonality.FRIENDLY).build();
        when(pets.findByIdAndMemberId(10L, 1L)).thenReturn(Optional.of(pet));
        when(images.readUrl("pet-images/photo.png")).thenReturn("https://signed.example/photo");
        assertThat(service.get(1L, 10L).profileImageUrl()).isEqualTo("https://signed.example/photo");
        assertThat(pet.getProfileImageUrl()).isEqualTo("pet-images/photo.png");
    }

    @Test
    void nonOwnerCannotTriggerSigning() {
        when(pets.findByIdAndMemberId(10L, 2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(2L, 10L)).isInstanceOf(GeneralException.class);
        verifyNoInteractions(images);
    }
}
