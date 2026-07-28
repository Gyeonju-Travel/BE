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
        message.setText("인증번호는 " + verificationCode + "입니다. "
                + expirationMinutes + "분 안에 입력해 주세요.");

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            throw new GeneralException(MemberErrorCode.VERIFICATION_EMAIL_SEND_FAILED, exception);
        }
    }
}
