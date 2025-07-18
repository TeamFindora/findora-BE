package com.findora.findora.common;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuccessResponse {
    @Schema(description = "성공 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private String message;

    @Schema(description = "성공 시각", example = "2025-07-07T14:30:00")
    private LocalDateTime timestamp;

    public static SuccessResponse of(String message) {
        return SuccessResponse.builder()
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
} 