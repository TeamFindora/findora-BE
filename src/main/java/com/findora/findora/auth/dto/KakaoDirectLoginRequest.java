package com.findora.findora.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "카카오 직접 로그인 요청 (프론트엔드에서 사용자 정보 직접 전달)")
public class KakaoDirectLoginRequest {
    
    @Schema(description = "카카오 사용자 고유 ID", example = "1234567890", required = true)
    private String kakaoId;
    
    @Schema(description = "카카오 계정 이메일", example = "user@kakao.com", required = true)
    private String email;
    
    @Schema(description = "카카오 프로필 닉네임", example = "카카오유저", required = true)
    private String nickname;
    
    @Schema(description = "카카오 액세스 토큰", example = "AAAAAgN...", required = true)
    private String accessToken;
} 