package com.example.gyeonjutravel.domain.pet.entity;

import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.pet.entity.enums.*;
import com.example.gyeonjutravel.global.common.BaseEntity;
import jakarta.persistence.Column;
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

@Getter
@Entity
@Table(name = "pets")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(length = 2048)
    private String profileImageUrl;

    @Column(length = 50)
    private String breed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DogSize size;

    @Column
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private PetGender gender;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PetPersonality personality;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TravelPreference travelPreference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalkingStyle walkingStyle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder
    private Pet(
            Member member,
            String name,
            String profileImageUrl,
            DogSize size,
            TravelPreference travelPreference,
            WalkingStyle walkingStyle
    ) {
        this.member = member;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.size = size;
        this.travelPreference = travelPreference;
        this.walkingStyle = walkingStyle;
    }

    public void updateProfile(
            String name,
            String profileImageUrl,
            String breed,
            DogSize size,
            Integer age,
            PetGender gender,
            PetPersonality personality
    ) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.breed = breed;
        this.size = size;
        this.age = age;
        this.gender = gender;
        this.personality = personality;
    }
}
