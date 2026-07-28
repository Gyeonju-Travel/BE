package com.example.gyeonjutravel.domain.place.entity;

import com.example.gyeonjutravel.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "places",
        uniqueConstraints = @UniqueConstraint(name = "uk_places_source_key", columnNames = "source_key")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_key", nullable = false, length = 80)
    private String sourceKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PlaceCategory category;

    @Column(nullable = false, length = 20)
    private String originalCategory;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 50)
    private String area;

    @Column(length = 50)
    private String administrativeDistrict;

    @Column(length = 50)
    private String detailCategory;

    @Column(nullable = false, length = 300)
    private String roadAddress;

    @Column(length = 300)
    private String lotAddress;

    @Column(length = 10)
    private String postalCode;

    @Column(length = 100)
    private String phone;

    @Column(length = 200)
    private String businessHours;

    @Column(length = 100)
    private String breakTime;

    @Column(length = 100)
    private String closedDays;

    @Column(length = 200)
    private String hoursNote;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double latitude;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 100)
    private String petAccessType;

    @Column(length = 200)
    private String allowedPets;

    @Column(length = 300)
    private String petRequirements;

    @Lob
    private String petInfo;

    @Column(length = 300)
    private String petFacilities;

    @Column(length = 300)
    private String petProvidedItems;

    @Lob
    private String petSafetyInfo;

    @Builder
    private Place(
            String sourceKey,
            PlaceCategory category,
            String originalCategory,
            String name,
            String area,
            String administrativeDistrict,
            String detailCategory,
            String roadAddress,
            String lotAddress,
            String postalCode,
            String phone,
            String businessHours,
            String breakTime,
            String closedDays,
            String hoursNote,
            Double longitude,
            Double latitude,
            String imageUrl,
            String petAccessType,
            String allowedPets,
            String petRequirements,
            String petInfo,
            String petFacilities,
            String petProvidedItems,
            String petSafetyInfo
    ) {
        this.sourceKey = sourceKey;
        this.category = category;
        this.originalCategory = originalCategory;
        this.name = name;
        this.area = area;
        this.administrativeDistrict = administrativeDistrict;
        this.detailCategory = detailCategory;
        this.roadAddress = roadAddress;
        this.lotAddress = lotAddress;
        this.postalCode = postalCode;
        this.phone = phone;
        this.businessHours = businessHours;
        this.breakTime = breakTime;
        this.closedDays = closedDays;
        this.hoursNote = hoursNote;
        this.longitude = longitude;
        this.latitude = latitude;
        this.imageUrl = imageUrl;
        this.petAccessType = petAccessType;
        this.allowedPets = allowedPets;
        this.petRequirements = petRequirements;
        this.petInfo = petInfo;
        this.petFacilities = petFacilities;
        this.petProvidedItems = petProvidedItems;
        this.petSafetyInfo = petSafetyInfo;
    }
}
