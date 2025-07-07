package com.findora.findora.emailverification.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.findora.findora.common.email.EmailSender;
import com.findora.findora.emailverification.model.EmailVerification;
import com.findora.findora.emailverification.repository.EmailVerificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailSender emailSender;
    
    // 인증코드 만료 시간 (분)
    private static final int EXPIRATION_MINUTES = 10;
    
    /**
     * 인증코드 생성 및 이메일 발송
     */
    @Transactional
    public void sendCode(String email) {
        // 이미 인증된 이메일이면 예외
        if (emailVerificationRepository.existsByEmailAndVerifiedTrue(email)) {
            throw new IllegalArgumentException("이미 인증된 이메일입니다.");
        }

        // 새로운 6자리 인증코드 생성
        String code = String.format("%06d", new Random().nextInt(999999));
        
        // 기존 인증 요청이 있는지 확인
        EmailVerification verification = emailVerificationRepository.findByEmail(email)
                .map(existing -> {
                    // 기존 요청이 있으면 새로운 코드로 업데이트 (이전 코드 무효화)
                    existing.updateCode(code, EXPIRATION_MINUTES);
                    log.info("기존 인증코드 무효화 후 새로운 코드 발급: {}", email);
                    return existing;
                })
                .orElse(EmailVerification.builder()
                        .email(email)
                        .code(code)
                        .sentAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES))
                        .verified(false)
                        .build());

        emailVerificationRepository.save(verification);
        
        // 이메일 발송
        try {
            emailSender.send(email, "Findora 이메일 인증코드", 
                String.format("인증코드: %s\n\n%d분 후 만료됩니다.", code, EXPIRATION_MINUTES));
            log.info("인증코드 이메일 발송 성공: {}", email);
        } catch (Exception e) {
            log.error("인증코드 이메일 발송 실패: {}", email, e);
            throw new RuntimeException("이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    /**
     * 인증코드 검증
     */
    @Transactional
    public boolean verifyCode(String email, String code) {
        EmailVerification verification = emailVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청이 없습니다. 먼저 인증코드를 요청해주세요."));
        
        // 이미 인증된 경우
        if (verification.isVerified()) {
            throw new IllegalArgumentException("이미 인증이 완료된 이메일입니다.");
        }
        
        // 만료된 경우
        if (verification.isExpired()) {
            throw new IllegalArgumentException("인증코드가 만료되었습니다. 새로운 인증코드를 요청해주세요.");
        }
        
        // 인증코드가 일치하지 않는 경우
        if (!verification.getCode().equals(code)) {
            log.warn("잘못된 인증코드 입력: {} (이메일: {})", code, email);
            throw new IllegalArgumentException("인증코드가 일치하지 않습니다.");
        }
        
        // 인증 성공 처리
        verification.markAsVerified();
        emailVerificationRepository.save(verification);
        
        log.info("이메일 인증 성공: {}", email);
        return true;
    }

    /**
     * 인증 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isVerified(String email) {
        return emailVerificationRepository.existsByEmailAndVerifiedTrue(email);
    }
    
    /**
     * 만료된 인증 요청들 정리 (배치나 스케줄러에서 사용)
     */
    @Transactional
    public void cleanupExpiredVerifications() {
        // 만료된 미인증 요청들을 삭제
        // 실제 구현은 요구사항에 따라 조정
        log.info("만료된 인증 요청 정리 작업 실행");
    }
    
    /**
     * 인증코드 만료 시간까지 남은 분 계산
     */
    public long getMinutesUntilExpiration(String email) {
        return emailVerificationRepository.findByEmail(email)
                .filter(v -> !v.isExpired())
                .map(v -> java.time.Duration.between(LocalDateTime.now(), v.getExpiresAt()).toMinutes())
                .orElse(0L);
    }
}