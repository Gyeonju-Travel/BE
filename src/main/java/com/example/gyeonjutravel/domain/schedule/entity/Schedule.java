package com.example.gyeonjutravel.domain.schedule.entity;

import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.global.common.BaseEntity;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "schedules")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "travel_date", nullable = false)
    private LocalDate travelDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "departure_area", nullable = false, length = 30)
    private DepartureArea departureArea;

    @Column(name = "started", nullable = false)
    private boolean started = false;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("visitOrder ASC")
    private List<ScheduleItem> items = new ArrayList<>();

    public Schedule(Member member, LocalDate travelDate, DepartureArea departureArea) {
        this.member = member;
        this.travelDate = travelDate;
        this.departureArea = departureArea;
    }

    public void updateDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public void updateRoute(DepartureArea departureArea) {
        this.departureArea = departureArea;
        this.items.clear();
    }

    public void start(LocalDateTime startedAt) {
        if (this.started) {
            return;
        }
        this.started = true;
        this.startedAt = startedAt;
    }

    public void cancelStart() {
        this.started = false;
        this.startedAt = null;
    }

    public void addItem(
            Place place,
            int visitOrder,
            long walkingDurationSeconds,
            long walkingDistanceMeters
    ) {
        addItem(
                place,
                visitOrder,
                Long.valueOf(walkingDurationSeconds),
                Long.valueOf(walkingDistanceMeters)
        );
    }

    public void addItem(
            Place place,
            int visitOrder,
            Long walkingDurationSeconds,
            Long walkingDistanceMeters
    ) {
        items.add(new ScheduleItem(
                this,
                place,
                visitOrder,
                walkingDurationSeconds,
                walkingDistanceMeters
        ));
    }
}
