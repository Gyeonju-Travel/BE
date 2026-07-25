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

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

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
    private Member(String email, String password, String nickname, String phoneNumber, Role role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public boolean addBookmark(Place place) {
        return bookmarkedPlaces.add(place);
    }

    public boolean removeBookmark(Long placeId) {
        return bookmarkedPlaces.removeIf(place -> place.getId().equals(placeId));
    }
}
