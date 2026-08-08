package com.example.gyeonjutravel.domain.report.repository;

import com.example.gyeonjutravel.domain.report.entity.PlaceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceReportRepository extends JpaRepository<PlaceReport, Long> {

    @Modifying
    @Query(value = """
            delete from place_report_pet_policies
            where place_report_id in (
                select id from place_reports where member_id = :memberId
            )
            """, nativeQuery = true)
    void deletePetPoliciesByMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query(value = "delete from place_reports where member_id = :memberId", nativeQuery = true)
    void deleteAllByMemberId(@Param("memberId") Long memberId);
}
