package com.findora.findora.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "토큰 갱신 요청")
public class TokenRefreshRequest {
    
    @Schema(description = "리프레시 토큰", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank(message = "리프레시 토큰은 필수입니다")
    private String refreshToken;
} 