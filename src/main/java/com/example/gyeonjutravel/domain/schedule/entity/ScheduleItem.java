package com.example.gyeonjutravel.domain.schedule.entity;

import com.example.gyeonjutravel.domain.place.entity.Place;
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

@Getter
@Entity
@Table(
        name = "schedule_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_schedule_items_schedule_order",
                columnNames = {"schedule_id", "visit_order"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "visit_order", nullable = false)
    private int visitOrder;

    @Column(name = "walking_duration_seconds", nullable = false)
    private long walkingDurationSeconds;

    @Column(name = "walking_distance_meters", nullable = false)
    private long walkingDistanceMeters;

    ScheduleItem(
            Schedule schedule,
            Place place,
            int visitOrder,
            long walkingDurationSeconds,
            long walkingDistanceMeters
    ) {
        this.schedule = schedule;
        this.place = place;
        this.visitOrder = visitOrder;
        this.walkingDurationSeconds = walkingDurationSeconds;
        this.walkingDistanceMeters = walkingDistanceMeters;
    }
}
