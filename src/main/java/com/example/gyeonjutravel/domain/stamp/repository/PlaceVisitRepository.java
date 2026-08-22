package com.example.gyeonjutravel.domain.stamp.repository;

import com.example.gyeonjutravel.domain.stamp.entity.PlaceVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaceVisitRepository extends JpaRepository<PlaceVisit, Long> {

    Optional<PlaceVisit> findByMemberIdAndScheduleIdAndPlaceId(Long memberId, Long scheduleId, Long placeId);

    long countDistinctByMemberIdAndScheduleId(Long memberId, Long scheduleId);

    List<PlaceVisit> findAllByMemberIdAndScheduleIdOrderByVisitedAtAsc(Long memberId, Long scheduleId);

    @Query("select visit from PlaceVisit visit "
            + "join fetch visit.place "
            + "where visit.member.id = :memberId "
            + "order by visit.visitedAt asc")
    List<PlaceVisit> findAllWithPlaceByMemberId(@Param("memberId") Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PlaceVisit visit "
            + "where visit.member.id = :memberId and visit.schedule.id in :scheduleIds")
    void deleteAllByMemberIdAndScheduleIdIn(
            @Param("memberId") Long memberId,
            @Param("scheduleIds") List<Long> scheduleIds
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PlaceVisit visit where visit.member.id = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);
}
