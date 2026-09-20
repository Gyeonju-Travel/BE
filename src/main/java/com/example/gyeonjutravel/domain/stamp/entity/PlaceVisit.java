package com.example.gyeonjutravel.domain.stamp.entity;

import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.schedule.entity.Schedule;
import com.example.gyeonjutravel.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "place_visits",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_place_visits_member_schedule_place",
                columnNames = {"member_id", "schedule_id", "place_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceVisit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "visited_at", nullable = false)
    private LocalDateTime visitedAt;

    // 획득 당시의 스탬프 종류를 저장하여 API 이름 변경이나 장애와 무관하게 기록을 조회합니다.
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "stamp_type", nullable = false, length = 40)
    private StampType stampType;

    public PlaceVisit(Member member, Schedule schedule, Place place, StampType stampType, LocalDateTime visitedAt) {
        this.member = member;
        this.schedule = schedule;
        this.place = place;
        this.visitedAt = visitedAt;
        this.stampType = java.util.Objects.requireNonNull(stampType);
    }
}
