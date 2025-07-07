package com.findora.findora.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "소셜 로그인 요청")
public class SocialLoginRequest {
    @Schema(description = "인가 코드", example = "abcd1234567890", required = true)
    private String authorizationCode;
    
    @Schema(description = "리다이렉트 URI", example = "http://localhost:8080/api/auth/social/oauth/kakao/code", required = true)
    private String redirectUri;
} 