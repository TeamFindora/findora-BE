package com.findora.findora.auth.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.findora.findora.auth.model.RefreshToken;
import com.findora.findora.auth.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
    
    private final RefreshTokenRepository refreshTokenRepository;
    
    /**
     * 새로운 Refresh Token 생성 및 저장
     */
    @Transactional
    public RefreshToken createRefreshToken(String userEmail, String clientIp) {
        // 기존 활성화된 토큰들 무효화
        revokeAllUserTokens(userEmail, clientIp, "새 토큰 발급");
        
        // 새 토큰 생성
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7); // 7일
        
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .userEmail(userEmail)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .revoked(false)
                .createdByIp(clientIp)
                .build();
        
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for user: {}", userEmail);
        
        return savedToken;
    }
    
    /**
     * Refresh Token 검증
     */
    public Optional<RefreshToken> validateRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(refreshToken -> !refreshToken.isRevoked())
                .filter(refreshToken -> refreshToken.getExpiresAt().isAfter(LocalDateTime.now()));
    }
    
    /**
     * 사용자의 모든 토큰 무효화 (로그아웃)
     */
    @Transactional
    public void revokeAllUserTokens(String userEmail, String clientIp, String reason) {
        refreshTokenRepository.revokeAllUserTokens(userEmail, clientIp, reason);
        log.info("Revoked all tokens for user: {}, reason: {}", userEmail, reason);
    }
    
    /**
     * 특정 토큰 무효화
     */
    @Transactional
    public void revokeToken(String token, String clientIp, String reason) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshToken.setRevokedByIp(clientIp);
                    refreshToken.setReasonRevoked(reason);
                    refreshTokenRepository.save(refreshToken);
                    log.info("Revoked token for user: {}, reason: {}", refreshToken.getUserEmail(), reason);
                });
    }
    
    /**
     * 사용자의 활성화된 토큰들 조회
     */
    public List<RefreshToken> getActiveTokensByUserEmail(String userEmail) {
        return refreshTokenRepository.findActiveTokensByUserEmail(userEmail, LocalDateTime.now());
    }
    
    /**
     * 만료된 토큰들 정리 (스케줄링)
     */
    @Scheduled(cron = "0 0 2 * * ?") // 매일 새벽 2시에 실행
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteExpiredTokens(now);
        log.info("Cleaned up expired refresh tokens");
    }
    
    /**
     * 사용자의 모든 토큰 삭제 (계정 삭제 시)
     */
    @Transactional
    public void deleteAllUserTokens(String userEmail) {
        refreshTokenRepository.deleteByUserEmail(userEmail);
        log.info("Deleted all tokens for user: {}", userEmail);
    }
} 