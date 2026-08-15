package com.example.gyeonjutravel.domain.notification.repository;

import com.example.gyeonjutravel.domain.notification.entity.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    Optional<NotificationSetting> findByMemberId(Long memberId);

    @Modifying
    @Query(value = "delete from notification_settings where member_id = :memberId", nativeQuery = true)
    void deleteByMemberId(@Param("memberId") Long memberId);
}
