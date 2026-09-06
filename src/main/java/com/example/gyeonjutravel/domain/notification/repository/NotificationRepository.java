package com.example.gyeonjutravel.domain.notification.repository;

import com.example.gyeonjutravel.domain.notification.entity.Notification;
import com.example.gyeonjutravel.domain.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByMemberIdAndScheduleIdAndType(Long memberId, Long scheduleId, NotificationType type);

    Optional<Notification> findByIdAndMemberId(Long id, Long memberId);

    Optional<Notification> findByMemberIdAndScheduleIdAndType(
            Long memberId,
            Long scheduleId,
            NotificationType type
    );

    @Query("select notification from Notification notification "
            + "join fetch notification.schedule "
            + "where notification.member.id = :memberId "
            + "order by notification.scheduledAt desc, notification.id desc")
    List<Notification> findAllByMemberIdOrderByScheduledAtDesc(@Param("memberId") Long memberId);

    long countByMemberIdAndReadAtIsNull(Long memberId);

    @Modifying
    @Query(value = "delete from notifications where member_id = :memberId", nativeQuery = true)
    void deleteAllByMemberId(@Param("memberId") Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Notification notification "
            + "where notification.member.id = :memberId and notification.schedule.id in :scheduleIds")
    void deleteAllByMemberIdAndScheduleIdIn(
            @Param("memberId") Long memberId,
            @Param("scheduleIds") List<Long> scheduleIds
    );
}
