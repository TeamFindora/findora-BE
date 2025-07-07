package com.findora.findora.auth.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.findora.findora.auth.dto.KakaoDirectLoginRequest;
import com.findora.findora.auth.dto.LoginResponse;
import com.findora.findora.auth.dto.SocialLoginRequest;
import com.findora.findora.auth.service.SocialAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "소셜 로그인", description = "카카오 등 소셜 로그인 API")
public class SocialAuthController {
    
    private final SocialAuthService socialAuthService;
    
    @PostMapping("/kakao")
    @Operation(summary = "카카오 직접 로그인", description = "프론트엔드에서 받은 카카오 사용자 정보로 로그인하거나 회원가입합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = LoginResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                      "refreshToken": "550e8400-e29b-41d4-a716...",
                      "tokenType": "Bearer",
                      "expiresIn": 86400000,
                      "user": {
                        "userId": 12,
                        "loginId": "kakao_1234567890",
                        "email": "user@kakao.com",
                        "nickname": "카카오유저",
                        "role": "STUDENT"
                      }
                    }
                """))),
        @ApiResponse(responseCode = "400", description = "로그인 실패",
            content = @Content(examples = @ExampleObject(value = """
                {
                    "error": "Invalid user information"
                }
                """)))
    })
    public ResponseEntity<LoginResponse> kakaoDirectLogin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "카카오 사용자 정보",
                required = true,
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = """
                        {
                          "kakaoId": "1234567890",
                          "email": "user@kakao.com",
                          "nickname": "카카오유저",
                          "accessToken": "AAAAAgN..."
                        }
                    """)))
            @RequestBody KakaoDirectLoginRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            String clientIp = getClientIpAddress(httpRequest);
            LoginResponse response = socialAuthService.kakaoDirectLogin(request, clientIp);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("카카오 직접 로그인 실패", e);
            throw new IllegalArgumentException("카카오 로그인에 실패했습니다: " + e.getMessage());
        }
    }
    
    @GetMapping("/social/oauth/kakao/code")
    @Operation(summary = "카카오 OAuth 콜백", description = "카카오에서 리다이렉트되는 콜백 엔드포인트입니다.")
    public void kakaoCallback(
            @RequestParam("code") String authorizationCode,
            @RequestParam(value = "state", required = false) String state,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        try {
            String clientIp = getClientIpAddress(request);
            String redirectUri = "http://localhost:8080/api/auth/social/oauth/kakao/code";
            
            // SocialLoginRequest 생성
            SocialLoginRequest socialLoginRequest = new SocialLoginRequest();
            socialLoginRequest.setAuthorizationCode(authorizationCode);
            socialLoginRequest.setRedirectUri(redirectUri);
            
            // 카카오 로그인 처리
            LoginResponse loginResponse = socialAuthService.kakaoLogin(socialLoginRequest, clientIp);
            
            // 성공 페이지로 리다이렉트 (토큰 정보 포함)
            String successUrl = String.format(
                "http://localhost:3000/auth/callback?accessToken=%s&refreshToken=%s&userId=%d&nickname=%s",
                loginResponse.getAccessToken(),
                loginResponse.getRefreshToken(),
                loginResponse.getUser().getUserId(),
                loginResponse.getUser().getNickname()
            );
            
            response.sendRedirect(successUrl);
            
        } catch (Exception e) {
            log.error("카카오 OAuth 콜백 처리 실패", e);
            // 에러 페이지로 리다이렉트
            response.sendRedirect("http://localhost:8080/login-error.html?error=" + e.getMessage());
        }
    }
    
    @PostMapping("/social/kakao")
    @Operation(summary = "카카오 로그인 (인가코드)", description = "카카오 인가 코드를 사용하여 로그인하거나 회원가입합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = LoginResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                      "refreshToken": "550e8400-e29b-41d4-a716...",
                      "tokenType": "Bearer",
                      "expiresIn": 86400000,
                      "user": {
                        "userId": 12,
                        "loginId": "kakao_1234567890",
                        "email": "user@kakao.com",
                        "nickname": "카카오유저",
                        "role": "STUDENT"
                      }
                    }
                """))),
        @ApiResponse(responseCode = "400", description = "로그인 실패",
            content = @Content(examples = @ExampleObject(value = """
                {
                    "error": "Invalid authorization code"
                }
                """)))
    })
    public ResponseEntity<LoginResponse> kakaoLogin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "카카오 로그인 요청",
                required = true,
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = """
                        {
                          "authorizationCode": "abcd1234567890",
                          "redirectUri": "http://localhost:8080/api/auth/social/oauth/kakao/code"
                        }
                    """)))
            @RequestBody SocialLoginRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            String clientIp = getClientIpAddress(httpRequest);
            LoginResponse response = socialAuthService.kakaoLogin(request, clientIp);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("카카오 로그인 실패", e);
            throw new IllegalArgumentException("카카오 로그인에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 