package com.example.gyeonjutravel.domain.report.entity;

import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.report.entity.enums.PetPolicy;
import com.example.gyeonjutravel.domain.report.entity.enums.PlaceReportStatus;
import com.example.gyeonjutravel.global.common.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Entity
@Table(name = "place_reports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false, length = 100)
    private String placeName;

    @Column(nullable = false, length = 255)
    private String address;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "place_report_pet_policies", joinColumns = @JoinColumn(name = "place_report_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "pet_policy", nullable = false, length = 30)
    private Set<PetPolicy> petPolicies = new LinkedHashSet<>();

    @Column(length = 2048)
    private String imageUrl;

    @Column(length = 1000)
    private String recommendationReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlaceReportStatus status;

    @Builder
    private PlaceReport(
            Member member,
            String placeName,
            String address,
            Set<PetPolicy> petPolicies,
            String imageUrl,
            String recommendationReason,
            PlaceReportStatus status
    ) {
        this.member = member;
        this.placeName = placeName;
        this.address = address;
        this.petPolicies = new LinkedHashSet<>(petPolicies);
        this.imageUrl = imageUrl;
        this.recommendationReason = recommendationReason;
        this.status = status;
    }
}
