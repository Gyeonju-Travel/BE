package com.example.gyeonjutravel.domain.member.repository;

import com.example.gyeonjutravel.domain.member.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    boolean existsByToken(String token);
}
