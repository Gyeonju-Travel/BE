package com.example.gyeonjutravel.domain.notification.service;

import com.example.gyeonjutravel.domain.notification.entity.FcmToken;
import com.example.gyeonjutravel.domain.notification.entity.Notification;
import com.example.gyeonjutravel.domain.notification.repository.FcmTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmPushService {

    private final FirebaseMessaging firebaseMessaging;
    private final FcmTokenRepository fcmTokenRepository;

    public void send(Notification notification) {
        fcmTokenRepository.findAllByMemberId(notification.getMember().getId())
                .forEach(token -> send(notification, token));
    }

    private void send(Notification notification, FcmToken token) {
        Message message = Message.builder()
                .setToken(token.getToken())
                .setNotification(com.google.firebase.messaging.Notification.builder()
                        .setTitle(notification.getTitle())
                        .setBody(notification.getBody())
                        .build())
                .putData("notificationId", String.valueOf(notification.getId()))
                .putData("type", notification.getType().name())
                .putData("targetUrl", notification.getTargetUrl())
                .build();
        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException exception) {
            log.warn("FCM 알림 발송에 실패했습니다. memberId={}, notificationId={}",
                    notification.getMember().getId(), notification.getId(), exception);
            if (isInvalidToken(exception)) {
                fcmTokenRepository.delete(token);
            }
        }
    }

    private boolean isInvalidToken(FirebaseMessagingException exception) {
        return exception.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                || exception.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT;
    }
}
