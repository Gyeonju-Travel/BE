package com.example.gyeonjutravel.domain.inquiry.repository;

import com.example.gyeonjutravel.domain.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
}
