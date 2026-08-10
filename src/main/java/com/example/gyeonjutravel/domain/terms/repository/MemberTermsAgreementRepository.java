package com.example.gyeonjutravel.domain.terms.repository;

import com.example.gyeonjutravel.domain.terms.entity.MemberTermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberTermsAgreementRepository extends JpaRepository<MemberTermsAgreement, Long> {

    Optional<MemberTermsAgreement> findByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);
}
