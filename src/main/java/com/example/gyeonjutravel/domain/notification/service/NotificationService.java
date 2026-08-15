package com.example.gyeonjutravel.domain.notification.service;

import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.member.exception.MemberErrorCode;
import com.example.gyeonjutravel.domain.member.repository.MemberRepository;
import com.example.gyeonjutravel.domain.notification.dto.request.NotificationSettingUpdateRequest;
import com.example.gyeonjutravel.domain.notification.dto.response.NotificationListItemResponse;
import com.example.gyeonjutravel.domain.notification.dto.response.NotificationListResponse;
import com.example.gyeonjutravel.domain.notification.dto.response.NotificationResponse;
import com.example.gyeonjutravel.domain.notification.dto.response.NotificationSettingResponse;
import com.example.gyeonjutravel.domain.notification.entity.Notification;
import com.example.gyeonjutravel.domain.notification.entity.NotificationSetting;
import com.example.gyeonjutravel.domain.notification.entity.NotificationType;
import com.example.gyeonjutravel.domain.notification.exception.NotificationErrorCode;
import com.example.gyeonjutravel.domain.notification.repository.NotificationRepository;
import com.example.gyeonjutravel.domain.notification.repository.NotificationSettingRepository;
import com.example.gyeonjutravel.domain.schedule.entity.Schedule;
import com.example.gyeonjutravel.domain.schedule.exception.ScheduleErrorCode;
import com.example.gyeonjutravel.domain.schedule.repository.ScheduleRepository;
import com.example.gyeonjutravel.domain.stamp.exception.StampErrorCode;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final LocalTime STAMP_ALBUM_READY_TIME = LocalTime.of(21, 0);

    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final ScheduleRepository scheduleRepository;

    public NotificationListResponse getNotifications(Long memberId) {
        return new NotificationListResponse(
                notificationRepository.countByMemberIdAndReadAtIsNull(memberId),
                notificationRepository.findAllByMemberIdOrderByScheduledAtDesc(memberId).stream()
                        .map(NotificationListItemResponse::from)
                        .toList()
        );
    }

    @Transactional
    public NotificationSettingResponse getSetting(Long memberId) {
        return NotificationSettingResponse.from(findOrCreateSetting(memberId));
    }

    @Transactional
    public NotificationSettingResponse updateSetting(Long memberId, NotificationSettingUpdateRequest request) {
        NotificationSetting setting = findOrCreateSetting(memberId);
        setting.updateEnabled(request.enabled());
        return NotificationSettingResponse.from(setting);
    }

    @Transactional
    public NotificationListItemResponse markAsRead(Long memberId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndMemberId(notificationId, memberId)
                .orElseThrow(() -> new GeneralException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
        notification.markAsRead(LocalDateTime.now());
        return NotificationListItemResponse.from(notification);
    }

    @Transactional
    public NotificationResponse sendStampAlbumReadyNotification(Long memberId, Long scheduleId) {
        Schedule schedule = scheduleRepository.findByIdAndMemberId(scheduleId, memberId)
                .orElseThrow(() -> new GeneralException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));
        validateStampAlbumNotificationSendable(schedule, LocalDateTime.now());
        if (!isNotificationEnabled(memberId)) {
            throw new GeneralException(NotificationErrorCode.NOTIFICATION_DISABLED);
        }
        return NotificationResponse.from(findOrCreateStampAlbumReadyNotification(schedule));
    }

    @Transactional
    public void createStampAlbumReadyNotificationIfAbsent(Schedule schedule) {
        if (!isNotificationEnabled(schedule.getMember().getId())) {
            return;
        }
        findOrCreateStampAlbumReadyNotification(schedule);
    }

    private Notification findOrCreateStampAlbumReadyNotification(Schedule schedule) {
        Long memberId = schedule.getMember().getId();
        return notificationRepository.findByMemberIdAndScheduleIdAndType(
                        memberId,
                        schedule.getId(),
                        NotificationType.STAMP_ALBUM_READY
                )
                .orElseGet(() -> notificationRepository.save(new Notification(
                schedule.getMember(),
                schedule,
                NotificationType.STAMP_ALBUM_READY,
                "일정이 종료됐나요?",
                "스크랩으로 오늘 하루를 기록해보세요.",
                "/api/schedules/" + schedule.getId() + "/stamp-album",
                schedule.getTravelDate().atTime(STAMP_ALBUM_READY_TIME)
        )));
    }

    private NotificationSetting findOrCreateSetting(Long memberId) {
        return notificationSettingRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    Member member = memberRepository.findById(memberId)
                            .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));
                    return notificationSettingRepository.save(new NotificationSetting(member));
                });
    }

    private boolean isNotificationEnabled(Long memberId) {
        return notificationSettingRepository.findByMemberId(memberId)
                .map(NotificationSetting::isEnabled)
                .orElse(true);
    }

    private void validateStampAlbumNotificationSendable(Schedule schedule, LocalDateTime requestedAt) {
        LocalDateTime readyAt = schedule.getTravelDate().atTime(STAMP_ALBUM_READY_TIME);
        if (!schedule.isStarted() || schedule.getStartedAt() == null) {
            throw new GeneralException(StampErrorCode.SCHEDULE_NOT_STARTED);
        }
        if (requestedAt.isBefore(readyAt)) {
            throw new GeneralException(StampErrorCode.STAMP_ALBUM_NOT_READY);
        }
    }
}
