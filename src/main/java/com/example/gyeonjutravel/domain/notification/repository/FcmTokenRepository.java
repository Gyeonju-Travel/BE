package com.example.gyeonjutravel.domain.notification.repository;

import com.example.gyeonjutravel.domain.notification.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByToken(String token);

    List<FcmToken> findAllByMemberId(Long memberId);

    void deleteByMemberIdAndToken(Long memberId, String token);

    void deleteAllByMemberId(Long memberId);
}
