package com.example.gyeonjutravel.domain.member.entity;

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
@Table(name = "password_reset_verifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String codeHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(length = 100)
    private String resetTokenHash;

    @Column
    private LocalDateTime resetTokenExpiresAt;

    @Builder
    private PasswordResetVerification(String email, String codeHash, LocalDateTime expiresAt) {
        this.email = email;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
    }

    public void update(String codeHash, LocalDateTime expiresAt) {
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.resetTokenHash = null;
        this.resetTokenExpiresAt = null;
    }

    public void verify(String resetTokenHash, LocalDateTime resetTokenExpiresAt) {
        this.resetTokenHash = resetTokenHash;
        this.resetTokenExpiresAt = resetTokenExpiresAt;
    }

    public boolean isExpired(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isResetTokenExpired(LocalDateTime now) {
        return resetTokenExpiresAt == null || !resetTokenExpiresAt.isAfter(now);
    }
}
