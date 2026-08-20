package com.example.gyeonjutravel.domain.stamp.repository;

import com.example.gyeonjutravel.domain.stamp.entity.EarnedStamp;
import com.example.gyeonjutravel.domain.stamp.entity.StampType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EarnedStampRepository extends JpaRepository<EarnedStamp, Long> {

    boolean existsByMemberIdAndStampType(Long memberId, StampType stampType);

    List<EarnedStamp> findAllByMemberIdOrderByEarnedAtAscIdAsc(Long memberId);
}
