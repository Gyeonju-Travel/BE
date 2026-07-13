package com.example.gyeonjutravel.domain.member.service;

import com.example.gyeonjutravel.domain.member.dto.request.MemberLoginRequest;
import com.example.gyeonjutravel.domain.member.dto.request.MemberSignUpRequest;
import com.example.gyeonjutravel.domain.member.dto.response.MemberAuthResponse;
import com.example.gyeonjutravel.domain.member.dto.response.MemberSignUpResponse;
import com.example.gyeonjutravel.domain.member.entity.BlacklistedToken;
import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.member.entity.Role;
import com.example.gyeonjutravel.domain.member.exception.MemberErrorCode;
import com.example.gyeonjutravel.domain.member.repository.BlacklistedTokenRepository;
import com.example.gyeonjutravel.domain.member.repository.MemberRepository;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.example.gyeonjutravel.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public MemberSignUpResponse signUp(MemberSignUpRequest request) {
        String email = request.email().toLowerCase();
        if (memberRepository.existsByEmail(email)) {
            throw new GeneralException(MemberErrorCode.DUPLICATE_EMAIL);
        }

        Member member = memberRepository.save(Member.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .phoneNumber(request.phoneNumber())
                .role(Role.USER)
                .build());

        return createSignUpResponse(member);
    }

    public MemberAuthResponse login(MemberLoginRequest request) {
        String email = request.email().toLowerCase();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.INVALID_LOGIN));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new GeneralException(MemberErrorCode.INVALID_LOGIN);
        }

        return createAuthResponse(member);
    }

    @Transactional
    public void logout(String token) {
        blacklistToken(token);
    }

    @Transactional
    public void withdraw(Member member, String token) {
        blacklistToken(token);
        memberRepository.delete(member);
    }

    private MemberAuthResponse createAuthResponse(Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(member);
        String refreshToken = jwtTokenProvider.createRefreshToken(member);
        return new MemberAuthResponse(
                member.getId(),
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpiresInSeconds(),
                jwtTokenProvider.getRefreshTokenExpiresInSeconds()
        );
    }

    private MemberSignUpResponse createSignUpResponse(Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(member);
        return new MemberSignUpResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getPhoneNumber(),
                accessToken,
                jwtTokenProvider.getAccessTokenExpiresInSeconds()
        );
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
