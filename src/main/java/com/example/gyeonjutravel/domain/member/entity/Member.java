package com.example.gyeonjutravel.domain.member.entity;

import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false, length = 30)
    private String name;

    @Column
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @ManyToMany
    @JoinTable(
            name = "place_bookmarks",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "place_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "place_bookmarks_member_place",
                    columnNames = {"member_id", "place_id"}
            )
    )
    private Set<Place> bookmarkedPlaces = new LinkedHashSet<>();

    @Builder
    private Member(
            String email,
            String password,
            String name,
            LocalDate birthDate,
            Gender gender,
            String phoneNumber
    ) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public boolean addBookmark(Place place) {
        return bookmarkedPlaces.add(place);
    }

    public boolean removeBookmarks(Set<Long> placeIds) {
        Set<Long> bookmarkedPlaceIds = bookmarkedPlaces.stream()
                .map(Place::getId)
                .collect(java.util.stream.Collectors.toSet());
        if (!bookmarkedPlaceIds.containsAll(placeIds)) {
            return false;
        }
        return bookmarkedPlaces.removeIf(place -> placeIds.contains(place.getId()));
    }
}
