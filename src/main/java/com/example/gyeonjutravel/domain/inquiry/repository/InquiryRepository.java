package com.example.gyeonjutravel.domain.inquiry.repository;

import com.example.gyeonjutravel.domain.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    @Modifying
    @Query(value = "delete from inquiries where member_id = :memberId", nativeQuery = true)
    void deleteAllByMemberId(@Param("memberId") Long memberId);
}
