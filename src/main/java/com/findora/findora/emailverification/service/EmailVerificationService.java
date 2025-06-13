package com.findora.findora.emailverification.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.findora.findora.common.email.EmailSender;
import com.findora.findora.emailverification.model.EmailVerification;
import com.findora.findora.emailverification.repository.EmailVerificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailSender emailSender;
    // 인증 토큰 생성 및 저장
    @Transactional
    public void sendCode(String email) {
        // 이미 인증된 이메일이면 예외
        if (emailVerificationRepository.existsByEmailAndVerifiedTrue(email)) {
            throw new IllegalArgumentException("이미 인증된 이메일입니다.");
        }

        // 이미 인증 요청 중이면 기존 코드 재사용 or 새로 발급
        String code = String.format("%06d", new Random().nextInt(999999));
        EmailVerification verification = emailVerificationRepository.findByEmail(email)
                .map(v -> {
                    v.setCode(code);
                    v.setSentAt(LocalDateTime.now());
                    v.setVerified(false);
                    return v;
                })
                .orElse(EmailVerification.builder()
                        .email(email)
                        .code(code)
                        .sentAt(LocalDateTime.now())
                        .verified(false)
                        .build());

        emailVerificationRepository.save(verification);
        emailSender.send(email, "이메일 인증코드", "인증코드: " + code);
    }

       // 인증코드 검증
    @Transactional
    public boolean verifyCode(String email, String code) {
        EmailVerification verification = emailVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청이 없습니다."));
        if (verification.isExpired()) return false;
        if (!verification.getCode().equals(code)) return false;
        verification.setVerified(true);
        return true;
    }

      // 인증여부 확인
    public boolean isVerified(String email) {
        return emailVerificationRepository.existsByEmailAndVerifiedTrue(email);
    }
}