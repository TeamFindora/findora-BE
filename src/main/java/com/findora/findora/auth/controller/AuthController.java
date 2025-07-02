package com.findora.findora.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.findora.findora.auth.dto.LoginRequest;
import com.findora.findora.auth.dto.LoginResponse;
import com.findora.findora.auth.dto.TokenRefreshRequest;
import com.findora.findora.auth.service.AuthService;
import com.findora.findora.auth.service.RefreshTokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "인증", description = "로그인, 로그아웃, 토큰 관리 API")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = LoginResponse.class),
                examples = @ExampleObject(value = """
                    {
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
                        "tokenType": "Bearer",
                        "expiresIn": 86400000
                    }
                    """))),
        @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(examples = @ExampleObject(value = """
                {
                    "message": "Invalid email or password"
                }
                """)))
    })
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        return ResponseEntity.ok(authService.login(loginRequest, clientIp));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 사용자의 모든 Refresh Token을 무효화합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그아웃 성공",
            content = @Content(examples = @ExampleObject(value = """
                {
                    "message": "Successfully logged out"
                }
                """))),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<String> logout(HttpServletRequest request, Authentication authentication) {
        String clientIp = getClientIpAddress(request);
        String userEmail = authentication.getName();
        
        // 사용자의 모든 Refresh Token 무효화
        refreshTokenService.revokeAllUserTokens(userEmail, clientIp, "로그아웃");
        
        log.info("User logged out: {}", userEmail);
        return ResponseEntity.ok("Successfully logged out");
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "토큰 갱신 실패")
    })
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody TokenRefreshRequest refreshRequest, HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        return ResponseEntity.ok(authService.refreshToken(refreshRequest.getRefreshToken(), clientIp));
    }

    @GetMapping("/validate")
    @Operation(summary = "토큰 검증", description = "현재 토큰의 유효성을 검증합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "토큰 유효",
            content = @Content(examples = @ExampleObject(value = """
                {
                    "valid": true,
                    "userEmail": "user@example.com"
                }
                """))),
        @ApiResponse(responseCode = "401", description = "토큰 무효")
    })
    public ResponseEntity<Object> validateToken(HttpServletRequest request, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(java.util.Map.of(
                "valid", true,
                "userEmail", authentication.getName()
            ));
        }
        return ResponseEntity.status(401).body("Invalid token");
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