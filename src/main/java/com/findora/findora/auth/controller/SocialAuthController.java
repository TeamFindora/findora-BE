package com.findora.findora.auth.controller;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/social")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "소셜 로그인", description = "카카오 등 소셜 로그인 API")
public class SocialAuthController {
    
    private final SocialAuthService socialAuthService;
    
    @PostMapping("/kakao")
    @Operation(summary = "카카오 로그인", description = "카카오 인가 코드를 사용하여 로그인하거나 회원가입합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = LoginResponse.class),
                examples = @ExampleObject(value = """
                    {
                      \"accessToken\": \"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJrYWthb18xMjM0NTY3ODkwIiwiaWF0IjoxNzUxNDUxODE0LCJleHAiOjE3NTE1MzgyMTR9.example\",
                      \"refreshToken\": \"550e8400-e29b-41d4-a716-446655440000\",
                      \"tokenType\": \"Bearer\",
                      \"expiresIn\": 86400000,
                      \"user\": {
                        \"userId\": 12,
                        \"loginId\": \"kakao_1234567890\",
                        \"email\": \"user@kakao.com\",
                        \"nickname\": \"카카오유저\",
                        \"role\": \"STUDENT\"
                      }
                    }
                """))),
        @ApiResponse(responseCode = "400", description = "로그인 실패",
            content = @Content(examples = @ExampleObject(value = """
                {
                    \"error\": \"Invalid authorization code\"
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
                          \"authorizationCode\": \"abcd1234567890\",
                          \"redirectUri\": \"http://localhost:3000/auth/callback\"
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