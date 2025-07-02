package com.findora.findora.auth.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.findora.findora.auth.model.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    // 토큰으로 RefreshToken 찾기
    Optional<RefreshToken> findByToken(String token);
    
    // 사용자의 활성화된 토큰들 찾기
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.userEmail = :userEmail AND rt.revoked = false AND rt.expiresAt > :now")
    java.util.List<RefreshToken> findActiveTokensByUserEmail(@Param("userEmail") String userEmail, @Param("now") LocalDateTime now);
    
    // 만료된 토큰들 삭제
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    // 사용자의 모든 토큰 삭제
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.userEmail = :userEmail")
    void deleteByUserEmail(@Param("userEmail") String userEmail);
    
    // 사용자의 모든 토큰을 무효화
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedByIp = :ip, rt.reasonRevoked = :reason WHERE rt.userEmail = :userEmail AND rt.revoked = false")
    void revokeAllUserTokens(@Param("userEmail") String userEmail, @Param("ip") String ip, @Param("reason") String reason);
    
    // 토큰이 존재하고 유효한지 확인
    boolean existsByTokenAndRevokedFalseAndExpiresAtAfter(String token, LocalDateTime now);
} 