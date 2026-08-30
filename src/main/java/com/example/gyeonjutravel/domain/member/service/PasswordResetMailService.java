package com.example.gyeonjutravel.domain.member.service;

import com.example.gyeonjutravel.domain.member.exception.MemberErrorCode;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordResetMailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public void sendVerificationCode(String email, String verificationCode, long expirationMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("[견주여행] 비밀번호 재설정 인증번호");
        message.setText("""
                안녕하세요, 견주여행입니다.

                비밀번호 재설정을 위한 인증번호를 안내드립니다.

                인증번호: %s

                해당 인증번호는 %d분 동안 유효하니, 시간 내에 입력해 주세요.
                본인이 요청하지 않은 메일이라면 이 메일을 무시해 주셔도 됩니다.

                감사합니다.
                견주여행 드림
                """.formatted(verificationCode, expirationMinutes));

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            throw new GeneralException(MemberErrorCode.VERIFICATION_EMAIL_SEND_FAILED, exception);
        }
    }
}
