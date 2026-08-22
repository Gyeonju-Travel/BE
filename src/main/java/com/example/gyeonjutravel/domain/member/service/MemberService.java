package com.example.gyeonjutravel.domain.member.service;

import com.example.gyeonjutravel.domain.inquiry.repository.InquiryRepository;
import com.example.gyeonjutravel.domain.member.dto.request.MemberLoginRequest;
import com.example.gyeonjutravel.domain.member.dto.request.MemberSignUpRequest;
import com.example.gyeonjutravel.domain.member.dto.request.PasswordResetCodeConfirmRequest;
import com.example.gyeonjutravel.domain.member.dto.request.PasswordResetRequest;
import com.example.gyeonjutravel.domain.member.dto.request.PasswordResetVerificationRequest;
import com.example.gyeonjutravel.domain.member.dto.response.MemberAuthResponse;
import com.example.gyeonjutravel.domain.member.dto.response.MemberSignUpResponse;
import com.example.gyeonjutravel.domain.member.dto.response.PasswordResetVerificationResponse;
import com.example.gyeonjutravel.domain.member.entity.BlacklistedToken;
import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.member.entity.PasswordResetVerification;
import com.example.gyeonjutravel.domain.member.exception.MemberErrorCode;
import com.example.gyeonjutravel.domain.member.repository.BlacklistedTokenRepository;
import com.example.gyeonjutravel.domain.member.repository.MemberRepository;
import com.example.gyeonjutravel.domain.member.repository.PasswordResetVerificationRepository;
import com.example.gyeonjutravel.domain.notification.repository.NotificationRepository;
import com.example.gyeonjutravel.domain.notification.repository.NotificationSettingRepository;
import com.example.gyeonjutravel.domain.notification.repository.FcmTokenRepository;
import com.example.gyeonjutravel.domain.pet.repository.PetRepository;
import com.example.gyeonjutravel.domain.report.repository.PlaceReportRepository;
import com.example.gyeonjutravel.domain.schedule.repository.ScheduleRepository;
import com.example.gyeonjutravel.domain.stamp.repository.PlaceVisitRepository;
import com.example.gyeonjutravel.domain.stamp.repository.StampAlbumRepository;
import com.example.gyeonjutravel.domain.terms.repository.MemberTermsAgreementRepository;
import com.example.gyeonjutravel.domain.terms.service.TermsService;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.example.gyeonjutravel.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int RESET_TOKEN_BYTE_LENGTH = 32;

    private final MemberRepository memberRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final PasswordResetVerificationRepository passwordResetVerificationRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final PetRepository petRepository;
    private final PlaceReportRepository placeReportRepository;
    private final ScheduleRepository scheduleRepository;
    private final PlaceVisitRepository placeVisitRepository;
    private final StampAlbumRepository stampAlbumRepository;
    private final InquiryRepository inquiryRepository;
    private final MemberTermsAgreementRepository memberTermsAgreementRepository;
    private final TermsService termsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordResetMailService passwordResetMailService;

    @Value("${app.password-reset.code-expiration-minutes:5}")
    private long passwordResetCodeExpirationMinutes;

    @Value("${app.password-reset.reset-token-expiration-minutes:10}")
    private long passwordResetTokenExpirationMinutes;

    @Transactional
    public MemberSignUpResponse signUp(MemberSignUpRequest request) {
        validatePasswordConfirmation(request.password(), request.passwordConfirmation());

        String email = normalizeEmail(request.email());
        if (memberRepository.existsByEmail(email)) {
            throw new GeneralException(MemberErrorCode.DUPLICATE_EMAIL);
        }

        Member member = memberRepository.save(Member.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .birthDate(request.birthDate())
                .gender(request.gender())
                .phoneNumber(request.phoneNumber())
                .build());

        termsService.assignSignUpAgreement(request.termsAgreementToken(), member);

        return createSignUpResponse(member);
    }

    public MemberAuthResponse login(MemberLoginRequest request) {
        String email = normalizeEmail(request.email());
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.INVALID_LOGIN));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new GeneralException(MemberErrorCode.INVALID_LOGIN);
        }

        return createAuthResponse(member);
    }

    @Transactional
    public void sendPasswordResetVerificationCode(PasswordResetVerificationRequest request) {
        String email = normalizeEmail(request.email());
        if (!memberRepository.existsByEmail(email)) {
            throw new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        String verificationCode = String.format(Locale.ROOT, "%06d", SECURE_RANDOM.nextInt(1_000_000));
        String codeHash = passwordEncoder.encode(verificationCode);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(passwordResetCodeExpirationMinutes);

        PasswordResetVerification verification = passwordResetVerificationRepository.findByEmail(email)
                .map(existing -> {
                    existing.update(codeHash, expiresAt);
                    return existing;
                })
                .orElseGet(() -> PasswordResetVerification.builder()
                        .email(email)
                        .codeHash(codeHash)
                        .expiresAt(expiresAt)
                        .build());
        passwordResetVerificationRepository.save(verification);
        passwordResetMailService.sendVerificationCode(
                email,
                verificationCode,
                passwordResetCodeExpirationMinutes
        );
    }

    @Transactional
    public PasswordResetVerificationResponse verifyPasswordResetCode(PasswordResetCodeConfirmRequest request) {
        String email = normalizeEmail(request.email());
        PasswordResetVerification verification = passwordResetVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.INVALID_VERIFICATION_CODE));

        if (verification.isExpired(LocalDateTime.now())) {
            throw new GeneralException(MemberErrorCode.EXPIRED_VERIFICATION_CODE);
        }
        if (!passwordEncoder.matches(request.verificationCode(), verification.getCodeHash())) {
            throw new GeneralException(MemberErrorCode.INVALID_VERIFICATION_CODE);
        }

        String resetToken = createResetToken();
        verification.verify(
                passwordEncoder.encode(resetToken),
                LocalDateTime.now().plusMinutes(passwordResetTokenExpirationMinutes)
        );
        return new PasswordResetVerificationResponse(
                resetToken,
                passwordResetTokenExpirationMinutes * 60
        );
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        validatePasswordConfirmation(request.newPassword(), request.newPasswordConfirmation());

        String email = normalizeEmail(request.email());
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));
        PasswordResetVerification verification = passwordResetVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.INVALID_PASSWORD_RESET_TOKEN));

        if (verification.getResetTokenHash() == null
                || !passwordEncoder.matches(request.resetToken(), verification.getResetTokenHash())) {
            throw new GeneralException(MemberErrorCode.INVALID_PASSWORD_RESET_TOKEN);
        }
        if (verification.isResetTokenExpired(LocalDateTime.now())) {
            throw new GeneralException(MemberErrorCode.EXPIRED_PASSWORD_RESET_TOKEN);
        }

        member.changePassword(passwordEncoder.encode(request.newPassword()));
        passwordResetVerificationRepository.delete(verification);
    }

    @Transactional
    public void logout(String token) {
        blacklistToken(token);
    }

    @Transactional
    public void withdraw(Member member, String token) {
        blacklistToken(token);
        memberRepository.deleteBookmarksByMemberId(member.getId());
        fcmTokenRepository.deleteAllByMemberId(member.getId());
        notificationRepository.deleteAllByMemberId(member.getId());
        notificationSettingRepository.deleteByMemberId(member.getId());
        placeVisitRepository.deleteAllByMemberId(member.getId());
        stampAlbumRepository.deletePhotosByMemberId(member.getId());
        stampAlbumRepository.deleteAllByMemberId(member.getId());
        scheduleRepository.deleteItemsByMemberId(member.getId());
        scheduleRepository.deleteAllByMemberId(member.getId());
        placeReportRepository.anonymizeMemberByMemberId(member.getId());
        inquiryRepository.deleteAllByMemberId(member.getId());
        petRepository.deleteAllByMemberId(member.getId());
        memberTermsAgreementRepository.deleteByMemberId(member.getId());
        memberRepository.delete(member);
        memberRepository.flush();
    }

    private MemberAuthResponse createAuthResponse(Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(member);
        String refreshToken = jwtTokenProvider.createRefreshToken(member);
        return new MemberAuthResponse(
                member.getId(),
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpiresInSeconds(),
                jwtTokenProvider.getRefreshTokenExpiresInSeconds(),
                petRepository.existsByMemberIdAndRepresentativeTrue(member.getId())
        );
    }

    private MemberSignUpResponse createSignUpResponse(Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(member);
        return new MemberSignUpResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getBirthDate(),
                member.getGender(),
                member.getPhoneNumber(),
                accessToken,
                jwtTokenProvider.getAccessTokenExpiresInSeconds(),
                false
        );
    }

    private void validatePasswordConfirmation(String password, String passwordConfirmation) {
        if (!password.equals(passwordConfirmation)) {
            throw new GeneralException(MemberErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String createResetToken() {
        byte[] tokenBytes = new byte[RESET_TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private void blacklistToken(String token) {
        if (!blacklistedTokenRepository.existsByToken(token)) {
            blacklistedTokenRepository.save(BlacklistedToken.builder()
                    .token(token)
                    .expiresAt(jwtTokenProvider.getExpiration(token))
                    .build());
        }
    }
}
