package com.example.gyeonjutravel.domain.schedule.repository;

import com.example.gyeonjutravel.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("select distinct schedule from Schedule schedule "
            + "left join fetch schedule.items item "
            + "left join fetch item.place "
            + "where schedule.member.id = :memberId and schedule.travelDate = :date "
            + "order by schedule.id asc, item.visitOrder asc")
    List<Schedule> findAllByMemberIdAndTravelDateWithItems(
            @Param("memberId") Long memberId,
            @Param("date") LocalDate date
    );

    @Query("select schedule from Schedule schedule "
            + "where schedule.member.id = :memberId and schedule.id in :scheduleIds")
    List<Schedule> findAllByMemberIdAndIdIn(
            @Param("memberId") Long memberId,
            @Param("scheduleIds") List<Long> scheduleIds
    );
}
