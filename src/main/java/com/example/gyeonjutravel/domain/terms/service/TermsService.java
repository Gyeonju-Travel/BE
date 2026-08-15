package com.example.gyeonjutravel.domain.terms.service;

import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.terms.dto.request.TermsAgreementRequest;
import com.example.gyeonjutravel.domain.terms.dto.response.SignUpTermsResponse;
import com.example.gyeonjutravel.domain.terms.dto.response.TermsAgreementResponse;
import com.example.gyeonjutravel.domain.terms.dto.response.TermsItemResponse;
import com.example.gyeonjutravel.domain.terms.entity.MemberTermsAgreement;
import com.example.gyeonjutravel.domain.terms.entity.SignUpTermsAgreement;
import com.example.gyeonjutravel.domain.terms.exception.TermsErrorCode;
import com.example.gyeonjutravel.domain.terms.repository.MemberTermsAgreementRepository;
import com.example.gyeonjutravel.domain.terms.repository.SignUpTermsAgreementRepository;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {

    private final MemberTermsAgreementRepository memberTermsAgreementRepository;
    private final SignUpTermsAgreementRepository signUpTermsAgreementRepository;

    public SignUpTermsResponse getSignUpTerms() {
        return new SignUpTermsResponse(List.of(
                new TermsItemResponse("TERMS_OF_SERVICE", "이용 약관 동의", true),
                new TermsItemResponse("PRIVACY_POLICY", "개인정보 수집 및 이용 동의", true),
                new TermsItemResponse("LOCATION_SERVICE", "위치기반 서비스 이용 동의", true),
                new TermsItemResponse("AGE_OVER_FOURTEEN", "만 14세 이상 사용자", true)
        ));
    }

    @Transactional
    public TermsAgreementResponse agreeSignUpTerms(TermsAgreementRequest request) {
        String agreementToken = UUID.randomUUID().toString();
        signUpTermsAgreementRepository.save(SignUpTermsAgreement.builder()
                .agreementToken(agreementToken)
                .termsOfServiceAgreed(request.termsOfServiceAgreed())
                .privacyPolicyAgreed(request.privacyPolicyAgreed())
                .locationServiceAgreed(request.locationServiceAgreed())
                .ageOverFourteenAgreed(request.ageOverFourteenAgreed())
                .agreedAt(LocalDateTime.now())
                .build());
        return new TermsAgreementResponse(agreementToken);
    }

    @Transactional
    public void assignSignUpAgreement(String agreementToken, Member member) {
        SignUpTermsAgreement signUpAgreement = signUpTermsAgreementRepository
                .findByAgreementTokenAndUsedFalse(agreementToken)
                .orElseThrow(() -> new GeneralException(TermsErrorCode.TERMS_AGREEMENT_NOT_FOUND));
        memberTermsAgreementRepository.save(MemberTermsAgreement.builder()
                .member(member)
                .agreementToken(signUpAgreement.getAgreementToken())
                .termsOfServiceAgreed(signUpAgreement.isTermsOfServiceAgreed())
                .privacyPolicyAgreed(signUpAgreement.isPrivacyPolicyAgreed())
                .locationServiceAgreed(signUpAgreement.isLocationServiceAgreed())
                .ageOverFourteenAgreed(signUpAgreement.isAgeOverFourteenAgreed())
                .agreedAt(signUpAgreement.getAgreedAt())
                .build());
        signUpAgreement.markUsed();
    }
}
