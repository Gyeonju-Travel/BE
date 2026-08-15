package com.example.gyeonjutravel.domain.notification.service;

import com.example.gyeonjutravel.domain.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class StampAlbumNotificationScheduler {

    private final ScheduleRepository scheduleRepository;
    private final NotificationService notificationService;

    @Transactional
    @Scheduled(cron = "0 0 21 * * *", zone = "Asia/Seoul")
    public void notifyStampAlbumReady() {
        scheduleRepository.findStartedSchedulesWithMemberByTravelDate(LocalDate.now())
                .forEach(notificationService::createStampAlbumReadyNotificationIfAbsent);
    }
}
