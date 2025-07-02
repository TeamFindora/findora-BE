package com.findora.findora.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "로그인 응답")
public class LoginResponse {
    
    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    
    @Schema(description = "리프레시 토큰", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;
    
    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType;
    
    @Schema(description = "토큰 만료 시간 (밀리초)", example = "86400000")
    private Long expiresIn;
    
    @Schema(description = "사용자 정보")
    private UserInfo user;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "사용자 정보")
    public static class UserInfo {
        @Schema(description = "사용자 ID", example = "12")
        private Long userId;
        
        @Schema(description = "로그인 ID", example = "testuser")
        private String loginId;
        
        @Schema(description = "이메일", example = "test@example.com")
        private String email;
        
        @Schema(description = "닉네임", example = "테스트유저")
        private String nickname;
        
        @Schema(description = "역할", example = "STUDENT")
        private String role;
    }
} 