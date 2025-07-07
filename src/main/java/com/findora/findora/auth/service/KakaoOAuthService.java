package com.findora.findora.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.findora.findora.auth.dto.KakaoTokenResponse;
import com.findora.findora.auth.dto.KakaoUserInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoOAuthService {
    
    @Value("${oauth.kakao.client-id}")
    private String clientId;
    
    @Value("${oauth.kakao.client-secret}")
    private String clientSecret;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    
    /**
     * 인가 코드로 카카오 토큰 받기
     */
    public KakaoTokenResponse getKakaoToken(String authorizationCode, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        
        // 클라이언트 시크릿이 있는 경우에만 추가 (카카오는 선택사항)
        if (clientSecret != null && !clientSecret.trim().isEmpty()) {
            params.add("client_secret", clientSecret);
        }
        
        params.add("redirect_uri", redirectUri);
        params.add("code", authorizationCode);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        
        ResponseEntity<KakaoTokenResponse> response = restTemplate.postForEntity(
            KAKAO_TOKEN_URL, request, KakaoTokenResponse.class);
        
        log.info("카카오 토큰 요청 성공");
        return response.getBody();
    }
    
    /**
     * 카카오 액세스 토큰으로 사용자 정보 조회
     */
    public KakaoUserInfo getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<KakaoUserInfo> response = restTemplate.exchange(
            KAKAO_USER_INFO_URL, HttpMethod.GET, request, KakaoUserInfo.class);
        
        log.info("카카오 사용자 정보 조회 성공: {}", response.getBody().getId());
        return response.getBody();
    }
} 