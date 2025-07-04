package com.findora.findora.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.findora.findora.auth.dto.LoginRequest;
import com.findora.findora.auth.dto.LoginResponse;
import com.findora.findora.auth.model.RefreshToken;
import com.findora.findora.users.model.User;
import com.findora.findora.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public LoginResponse login(LoginRequest loginRequest, String clientIp) {
        // 인증 시도
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getLoginId(),
                loginRequest.getPassword()
            )
        );

        // UserDetails 가져오기
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // JWT Access Token 생성
        String accessToken = jwtService.generateToken(userDetails);
        
        // Refresh Token 생성 및 DB 저장
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(loginRequest.getLoginId(), clientIp);
        
        log.info("User logged in successfully: {}", loginRequest.getLoginId());
        
        // 사용자 정보 조회
        User user = userRepository.findByLoginId(loginRequest.getLoginId())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // 사용자 정보 생성
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
            .userId(user.getId())
            .loginId(user.getLoginId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .role(user.getRole().name())
            .build();
        
        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken.getToken())
            .tokenType("Bearer")
            .expiresIn(86400000L) // 24시간
            .user(userInfo)
            .build();
    }

    public LoginResponse refreshToken(String refreshToken, String clientIp) {
        try {
            // Refresh Token 검증
            RefreshToken validRefreshToken = refreshTokenService.validateRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
            
            // 사용자 정보로 UserDetails 생성
            User user = userRepository.findByLoginId(validRefreshToken.getUserEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getLoginId())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
            
            // 기존 Refresh Token 무효화
            refreshTokenService.revokeToken(refreshToken, clientIp, "토큰 갱신");
            
            // 새로운 토큰들 생성
            String newAccessToken = jwtService.generateToken(userDetails);
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getLoginId(), clientIp);
            
            log.info("Token refreshed for user: {}", user.getLoginId());
            
            return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .build();
                
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Token refresh failed", e);
        }
    }

    /**
     * User 객체로 LoginResponse 생성 (소셜 로그인용)
     */
    public LoginResponse createLoginResponse(User user, String clientIp) {
        // UserDetails 생성
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
            .username(user.getLoginId())
            .password(user.getPassword())
            .roles(user.getRole().name())
            .build();
        
        // JWT Access Token 생성
        String accessToken = jwtService.generateToken(userDetails);
        
        // Refresh Token 생성 및 DB 저장
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getLoginId(), clientIp);
        
        log.info("Login response created for user: {}", user.getLoginId());
        
        // 사용자 정보 생성
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
            .userId(user.getId())
            .loginId(user.getLoginId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .role(user.getRole().name())
            .build();
        
        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken.getToken())
            .tokenType("Bearer")
            .expiresIn(86400000L) // 24시간
            .user(userInfo)
            .build();
    }
} 