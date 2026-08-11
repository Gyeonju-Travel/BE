package com.example.gyeonjutravel.domain.stamp.repository;

import com.example.gyeonjutravel.domain.stamp.entity.PlaceVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaceVisitRepository extends JpaRepository<PlaceVisit, Long> {

    Optional<PlaceVisit> findByMemberIdAndScheduleIdAndPlaceId(Long memberId, Long scheduleId, Long placeId);

    long countDistinctByMemberIdAndScheduleId(Long memberId, Long scheduleId);

    List<PlaceVisit> findAllByMemberIdAndScheduleIdOrderByVisitedAtAsc(Long memberId, Long scheduleId);
}
