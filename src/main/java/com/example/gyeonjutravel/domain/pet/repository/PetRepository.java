package com.example.gyeonjutravel.domain.pet.repository;

import com.example.gyeonjutravel.domain.pet.entity.Pet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findAllByMemberIdAndRepresentativeFalseOrderByIdAsc(Long memberId);

    Optional<Pet> findFirstByMemberIdAndRepresentativeTrue(Long memberId);

    Optional<Pet> findByIdAndMemberId(Long petId, Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pet p where p.member.id = :memberId order by p.id asc")
    List<Pet> findAllByMemberIdForUpdate(@Param("memberId") Long memberId);

    boolean existsByMemberIdAndRepresentativeTrue(Long memberId);

    void deleteAllByMemberId(Long memberId);
}
