package com.example.gyeonjutravel.domain.terms.repository;

import com.example.gyeonjutravel.domain.terms.entity.SignUpTermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignUpTermsAgreementRepository extends JpaRepository<SignUpTermsAgreement, Long> {

    Optional<SignUpTermsAgreement> findByAgreementTokenAndUsedFalse(String agreementToken);
}
