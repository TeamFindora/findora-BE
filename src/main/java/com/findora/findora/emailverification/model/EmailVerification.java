package com.findora.findora.emailverification.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "email_verification")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EmailVerification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean verified;

    @Column(nullable = true)
    private LocalDateTime verifiedAt;

    /**
     * 인증코드가 만료되었는지 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 인증코드가 유효한지 확인 (만료되지 않고 아직 인증되지 않음)
     */
    public boolean isValid() {
        return !isExpired() && !verified;
    }

    /**
     * 인증 완료 처리
     */
    public void markAsVerified() {
        this.verified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    /**
     * 새로운 인증코드로 업데이트 (기존 인증은 무효화)
     */
    public void updateCode(String newCode, int expirationMinutes) {
        this.code = newCode;
        this.sentAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
        this.verified = false; // 새 코드 발급 시 이전 인증 무효화
        this.verifiedAt = null;
    }
}