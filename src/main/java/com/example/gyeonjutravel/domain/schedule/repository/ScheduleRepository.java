package com.example.gyeonjutravel.domain.schedule.repository;

import com.example.gyeonjutravel.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    Optional<Schedule> findByIdAndMemberId(Long id, Long memberId);

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

    @Query("select distinct schedule from Schedule schedule "
            + "left join fetch schedule.items item "
            + "left join fetch item.place "
            + "where schedule.member.id = :memberId and schedule.started = true "
            + "order by schedule.travelDate desc, schedule.id desc")
    List<Schedule> findStartedSchedulesWithItemsByMemberId(@Param("memberId") Long memberId);

    @Query("select schedule from Schedule schedule "
            + "join fetch schedule.member "
            + "where schedule.travelDate = :date and schedule.started = true")
    List<Schedule> findStartedSchedulesWithMemberByTravelDate(@Param("date") LocalDate date);

    @Modifying
    @Query(value = """
            delete from schedule_items
            where schedule_id in (
                select id from schedules where member_id = :memberId
            )
            """, nativeQuery = true)
    void deleteItemsByMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query(value = "delete from schedules where member_id = :memberId", nativeQuery = true)
    void deleteAllByMemberId(@Param("memberId") Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            delete from schedule_items
            where schedule_id in (:scheduleIds)
            and schedule_id in (
                select id from schedules where member_id = :memberId
            )
            """, nativeQuery = true)
    void deleteItemsByMemberIdAndScheduleIdIn(
            @Param("memberId") Long memberId,
            @Param("scheduleIds") List<Long> scheduleIds
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "delete from schedules where member_id = :memberId and id in (:scheduleIds)", nativeQuery = true)
    void deleteAllByMemberIdAndIdIn(
            @Param("memberId") Long memberId,
            @Param("scheduleIds") List<Long> scheduleIds
    );
}
