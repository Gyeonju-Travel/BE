package com.example.gyeonjutravel.domain.report.repository;

import com.example.gyeonjutravel.domain.report.entity.PlaceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceReportRepository extends JpaRepository<PlaceReport, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "update place_reports set member_id = null where member_id = :memberId", nativeQuery = true)
    void anonymizeMemberByMemberId(@Param("memberId") Long memberId);
}
