package com.example.gyeonjutravel.domain.stamp.entity;

import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.place.entity.Place;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "earned_stamps",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_earned_stamps_member_type",
                columnNames = {"member_id", "stamp_type"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EarnedStamp extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "stamp_type", nullable = false, length = 40)
    private StampType stampType;

    @Column(name = "source_schedule_id")
    private Long sourceScheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_place_id")
    private Place sourcePlace;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    public EarnedStamp(
            Member member,
            StampType stampType,
            Long sourceScheduleId,
            Place sourcePlace,
            LocalDateTime earnedAt
    ) {
        this.member = member;
        this.stampType = stampType;
        this.sourceScheduleId = sourceScheduleId;
        this.sourcePlace = sourcePlace;
        this.earnedAt = earnedAt;
    }
}
