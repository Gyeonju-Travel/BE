package com.example.gyeonjutravel.domain.terms.entity;

import com.example.gyeonjutravel.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "signup_terms_agreements")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignUpTermsAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String agreementToken;

    @Column(nullable = false)
    private boolean termsOfServiceAgreed;

    @Column(nullable = false)
    private boolean privacyPolicyAgreed;

    @Column(nullable = false)
    private boolean locationServiceAgreed;

    @Column(nullable = false)
    private boolean ageOverFourteenAgreed;

    @Column(nullable = false)
    private LocalDateTime agreedAt;

    @Column(nullable = false)
    private boolean used;

    @Builder
    private SignUpTermsAgreement(
            String agreementToken,
            boolean termsOfServiceAgreed,
            boolean privacyPolicyAgreed,
            boolean locationServiceAgreed,
            boolean ageOverFourteenAgreed,
            LocalDateTime agreedAt
    ) {
        this.agreementToken = agreementToken;
        this.termsOfServiceAgreed = termsOfServiceAgreed;
        this.privacyPolicyAgreed = privacyPolicyAgreed;
        this.locationServiceAgreed = locationServiceAgreed;
        this.ageOverFourteenAgreed = ageOverFourteenAgreed;
        this.agreedAt = agreedAt;
    }

    public void markUsed() {
        this.used = true;
    }
}
