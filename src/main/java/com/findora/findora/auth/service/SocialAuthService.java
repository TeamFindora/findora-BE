package com.findora.findora.auth.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.findora.findora.auth.dto.KakaoDirectLoginRequest;
import com.findora.findora.auth.dto.KakaoTokenResponse;
import com.findora.findora.auth.dto.KakaoUserInfo;
import com.findora.findora.auth.dto.LoginResponse;
import com.findora.findora.auth.dto.SocialLoginRequest;
import com.findora.findora.auth.model.SocialAuth;
import com.findora.findora.auth.repository.SocialAuthRepository;
import com.findora.findora.users.model.User;
import com.findora.findora.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SocialAuthService {
    
    private final KakaoOAuthService kakaoOAuthService;
    private final SocialAuthRepository socialAuthRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 카카오 직접 로그인 처리 (프론트엔드에서 사용자 정보 직접 전달)
     */
    public LoginResponse kakaoDirectLogin(KakaoDirectLoginRequest request, String clientIp) {
        // 1. 기존 소셜 계정 확인
        Optional<SocialAuth> existingSocialAuth = socialAuthRepository
            .findByProviderAndProviderUserId("kakao", request.getKakaoId());
        
        User user;
        if (existingSocialAuth.isPresent()) {
            // 기존 사용자 - 액세스 토큰 업데이트
            user = existingSocialAuth.get().getUser();
            updateSocialAuthAccessToken(existingSocialAuth.get(), request.getAccessToken());
            log.info("기존 카카오 사용자 로그인: {}", user.getEmail());
        } else {
            // 신규 사용자 - 회원가입 후 소셜 계정 연결
            user = createUserFromKakaoInfo(request);
            createSocialAuthFromKakaoInfo(user, request);
            log.info("신규 카카오 사용자 가입 및 로그인: {}", user.getEmail());
        }
        
        // 2. JWT 토큰 생성 및 로그인 응답 반환
        return authService.createLoginResponse(user, clientIp);
    }
    
    /**
     * 카카오 로그인 처리 (인가코드 방식)
     */
    public LoginResponse kakaoLogin(SocialLoginRequest request, String clientIp) {
        // 1. 인가 코드로 카카오 토큰 받기
        KakaoTokenResponse tokenResponse = kakaoOAuthService.getKakaoToken(
            request.getAuthorizationCode(), request.getRedirectUri());
        
        // 2. 카카오 사용자 정보 조회
        KakaoUserInfo kakaoUserInfo = kakaoOAuthService.getKakaoUserInfo(tokenResponse.getAccessToken());
        
        // 3. 기존 소셜 계정 확인
        Optional<SocialAuth> existingSocialAuth = socialAuthRepository
            .findByProviderAndProviderUserId("kakao", kakaoUserInfo.getId().toString());
        
        User user;
        if (existingSocialAuth.isPresent()) {
            // 기존 사용자 - 토큰 정보 업데이트
            user = existingSocialAuth.get().getUser();
            updateSocialAuthTokens(existingSocialAuth.get(), tokenResponse);
            log.info("기존 카카오 사용자 로그인: {}", user.getEmail());
        } else {
            // 신규 사용자 - 회원가입 후 소셜 계정 연결
            user = createUserFromKakao(kakaoUserInfo);
            createSocialAuth(user, kakaoUserInfo, tokenResponse);
            log.info("신규 카카오 사용자 가입 및 로그인: {}", user.getEmail());
        }
        
        // 4. JWT 토큰 생성 및 로그인 응답 반환
        return authService.createLoginResponse(user, clientIp);
    }
    
    /**
     * 카카오 직접 정보로 새 계정 생성
     */
    private User createUserFromKakaoInfo(KakaoDirectLoginRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다: " + request.getEmail());
        }
        
        // 닉네임 중복 체크 및 고유 닉네임 생성
        String uniqueNickname = generateUniqueNickname(request.getNickname());
        
        User user = User.builder()
            .loginId("kakao_" + request.getKakaoId()) // 카카오 ID 기반 로그인 ID
            .email(request.getEmail())
            .password(passwordEncoder.encode(UUID.randomUUID().toString())) // 랜덤 비밀번호
            .nickname(uniqueNickname)
            .role(User.Role.STUDENT) // 기본 역할
            .emailVerified(true) // 카카오 인증된 이메일로 간주
            .build();
        
        return userRepository.save(user);
    }
    
    /**
     * 카카오 직접 정보로 소셜 인증 정보 생성
     */
    private void createSocialAuthFromKakaoInfo(User user, KakaoDirectLoginRequest request) {
        SocialAuth socialAuth = SocialAuth.builder()
            .user(user)
            .provider("kakao")
            .providerUserId(request.getKakaoId())
            .accessToken(request.getAccessToken())
            .refreshToken(null) // 프론트엔드에서는 리프레시 토큰이 없음
            .connectedAt(LocalDateTime.now())
            .build();
        
        socialAuthRepository.save(socialAuth);
    }
    
    /**
     * 소셜 인증 액세스 토큰만 업데이트
     */
    private void updateSocialAuthAccessToken(SocialAuth socialAuth, String accessToken) {
        SocialAuth updatedSocialAuth = SocialAuth.builder()
            .id(socialAuth.getId())
            .user(socialAuth.getUser())
            .provider(socialAuth.getProvider())
            .providerUserId(socialAuth.getProviderUserId())
            .accessToken(accessToken)
            .refreshToken(socialAuth.getRefreshToken()) // 기존 리프레시 토큰 유지
            .connectedAt(LocalDateTime.now())
            .build();
        
        socialAuthRepository.save(updatedSocialAuth);
    }
    
    /**
     * 카카오 사용자 정보로 새 계정 생성 (인가코드 방식)
     */
    private User createUserFromKakao(KakaoUserInfo kakaoUserInfo) {
        String email = kakaoUserInfo.getKakaoAccount().getEmail();
        String nickname = kakaoUserInfo.getProperties().getNickname();
        
        // 이메일 중복 체크
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다: " + email);
        }
        
        // 닉네임 중복 체크 및 고유 닉네임 생성
        String uniqueNickname = generateUniqueNickname(nickname);
        
        User user = User.builder()
            .loginId("kakao_" + kakaoUserInfo.getId()) // 카카오 ID 기반 로그인 ID
            .email(email)
            .password(passwordEncoder.encode(UUID.randomUUID().toString())) // 랜덤 비밀번호
            .nickname(uniqueNickname)
            .role(User.Role.STUDENT) // 기본 역할
            .emailVerified(true) // 카카오 인증된 이메일로 간주
            .build();
        
        return userRepository.save(user);
    }
    
    /**
     * 소셜 인증 정보 생성 (인가코드 방식)
     */
    private void createSocialAuth(User user, KakaoUserInfo kakaoUserInfo, KakaoTokenResponse tokenResponse) {
        SocialAuth socialAuth = SocialAuth.builder()
            .user(user)
            .provider("kakao")
            .providerUserId(kakaoUserInfo.getId().toString())
            .accessToken(tokenResponse.getAccessToken())
            .refreshToken(tokenResponse.getRefreshToken())
            .connectedAt(LocalDateTime.now())
            .build();
        
        socialAuthRepository.save(socialAuth);
    }
    
    /**
     * 소셜 인증 토큰 정보 업데이트 (인가코드 방식)
     */
    private void updateSocialAuthTokens(SocialAuth socialAuth, KakaoTokenResponse tokenResponse) {
        // 새로운 토큰으로 업데이트 (업데이트 메서드가 엔티티에 없으므로 새로 저장)
        SocialAuth updatedSocialAuth = SocialAuth.builder()
            .id(socialAuth.getId())
            .user(socialAuth.getUser())
            .provider(socialAuth.getProvider())
            .providerUserId(socialAuth.getProviderUserId())
            .accessToken(tokenResponse.getAccessToken())
            .refreshToken(tokenResponse.getRefreshToken())
            .connectedAt(LocalDateTime.now())
            .build();
        
        socialAuthRepository.save(updatedSocialAuth);
    }
    
    /**
     * 고유한 닉네임 생성
     */
    private String generateUniqueNickname(String baseNickname) {
        String nickname = baseNickname;
        int counter = 1;
        
        while (userRepository.existsByNickname(nickname)) {
            nickname = baseNickname + counter++;
        }
        
        return nickname;
    }
} 