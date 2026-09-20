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

    @Column(name = "tour_content_id", unique = true, length = 30)
    private String tourContentId;

    // 현재 요청에서만 사용하는 데이터이며, Hibernate가 API 응답을 DB에 저장하지 않도록 함.
    @jakarta.persistence.Transient
    private Place tourDetails;

    @jakarta.persistence.Transient
    private String overview;

    // 스탬프 대상 분류는 서비스 운영 정보이며, 관광지 소개 데이터를 저장하는 용도가 아님.
    @Enumerated(EnumType.STRING)
    @Column(name = "stamp_type", length = 40)
    private com.example.gyeonjutravel.domain.stamp.entity.StampType stampType;

    public void assignStampType(com.example.gyeonjutravel.domain.stamp.entity.StampType type) {
        if (stampType == null) stampType = type;
    }

    public String getOverview() { return tourDetails == null ? overview : tourDetails.overview; }

    public static Place tourReference(String sourceKey) {
        Place place = Place.builder().sourceKey(sourceKey).category(PlaceCategory.ATTRACTION).build();
        place.clearTourInformation();
        return place;
    }

    public void linkTourContent(String contentId) {
        if (tourContentId != null && !tourContentId.equals(contentId)) {
            throw new IllegalStateException("TourAPI identity cannot be reassigned");
        }
        tourContentId = contentId;
    }

    public void useTourDetails(Place details) {
        if (category != PlaceCategory.ATTRACTION || details == this) {
            throw new IllegalArgumentException("Invalid attraction details");
        }
        tourDetails = details;
    }

    public void clearTourInformation() {
        if (category != PlaceCategory.ATTRACTION) return;
        originalCategory = "관광지";
        name = "관광지 정보 조회 필요";
        roadAddress = "";
        longitude = 0.0;
        latitude = 0.0;
        area = administrativeDistrict = detailCategory = lotAddress = postalCode = phone = null;
        businessHours = breakTime = closedDays = hoursNote = imageUrl = null;
        petAccessType = allowedPets = petRequirements = petInfo = petFacilities = null;
        petProvidedItems = petSafetyInfo = null;
        tourDetails = null;
    }

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
            String petSafetyInfo,
            String overview
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
        this.overview = overview;
    }

    public String getOriginalCategory() { return tourDetails == null ? originalCategory : tourDetails.originalCategory; }
    public String getName() { return tourDetails == null ? name : tourDetails.name; }
    public String getArea() { return tourDetails == null ? area : tourDetails.area; }
    public String getAdministrativeDistrict() { return tourDetails == null ? administrativeDistrict : tourDetails.administrativeDistrict; }
    public String getDetailCategory() { return tourDetails == null ? detailCategory : tourDetails.detailCategory; }
    public String getRoadAddress() { return tourDetails == null ? roadAddress : tourDetails.roadAddress; }
    public String getLotAddress() { return tourDetails == null ? lotAddress : tourDetails.lotAddress; }
    public String getPostalCode() { return tourDetails == null ? postalCode : tourDetails.postalCode; }
    public String getPhone() { return tourDetails == null ? phone : tourDetails.phone; }
    public String getBusinessHours() { return tourDetails == null ? businessHours : tourDetails.businessHours; }
    public String getBreakTime() { return tourDetails == null ? breakTime : tourDetails.breakTime; }
    public String getClosedDays() { return tourDetails == null ? closedDays : tourDetails.closedDays; }
    public String getHoursNote() { return tourDetails == null ? hoursNote : tourDetails.hoursNote; }
    public Double getLongitude() { return tourDetails == null ? longitude : tourDetails.longitude; }
    public Double getLatitude() { return tourDetails == null ? latitude : tourDetails.latitude; }
    public String getImageUrl() { return tourDetails == null ? imageUrl : tourDetails.imageUrl; }
    public String getPetAccessType() { return tourDetails == null ? petAccessType : tourDetails.petAccessType; }
    public String getAllowedPets() { return tourDetails == null ? allowedPets : tourDetails.allowedPets; }
    public String getPetRequirements() { return tourDetails == null ? petRequirements : tourDetails.petRequirements; }
    public String getPetInfo() { return tourDetails == null ? petInfo : tourDetails.petInfo; }
    public String getPetFacilities() { return tourDetails == null ? petFacilities : tourDetails.petFacilities; }
    public String getPetProvidedItems() { return tourDetails == null ? petProvidedItems : tourDetails.petProvidedItems; }
    public String getPetSafetyInfo() { return tourDetails == null ? petSafetyInfo : tourDetails.petSafetyInfo; }
}
