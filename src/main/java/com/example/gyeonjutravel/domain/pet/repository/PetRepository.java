package com.example.gyeonjutravel.domain.pet.repository;

import com.example.gyeonjutravel.domain.pet.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findAllByMemberIdAndRepresentativeFalseOrderByIdAsc(Long memberId);

    Optional<Pet> findFirstByMemberIdAndRepresentativeTrue(Long memberId);

    Optional<Pet> findByIdAndMemberId(Long petId, Long memberId);

    boolean existsByMemberIdAndRepresentativeTrue(Long memberId);

    void deleteAllByMemberId(Long memberId);
}
