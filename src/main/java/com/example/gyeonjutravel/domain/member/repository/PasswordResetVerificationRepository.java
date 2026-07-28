package com.example.gyeonjutravel.domain.member.repository;

import com.example.gyeonjutravel.domain.member.entity.PasswordResetVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetVerificationRepository extends JpaRepository<PasswordResetVerification, Long> {

    Optional<PasswordResetVerification> findByEmail(String email);
}
