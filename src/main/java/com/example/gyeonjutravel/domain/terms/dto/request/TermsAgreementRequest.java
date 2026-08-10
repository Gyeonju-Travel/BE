package com.example.gyeonjutravel.domain.terms.dto.request;

import jakarta.validation.constraints.AssertTrue;

public record TermsAgreementRequest(
        @AssertTrue(message = "이용 약관에 동의해주세요.")
        boolean termsOfServiceAgreed,

        @AssertTrue(message = "개인정보 수집 및 이용에 동의해주세요.")
        boolean privacyPolicyAgreed,

        @AssertTrue(message = "위치기반 서비스 이용에 동의해주세요.")
        boolean locationServiceAgreed,

        @AssertTrue(message = "만 14세 이상 사용자만 가입할 수 있습니다.")
        boolean ageOverFourteenAgreed
) {
}
