package com.findora.findora.bookmark.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookmarkResponseDto {
    @Schema(description = "즐겨찾기 ID", example = "1")
    private Long id;

    @Schema(description = "게시글 ID", example = "10")
    private Long postId;

    @Schema(description = "게시글 제목", example = "Spring Boot 질문드립니다")
    private String postTitle;

    @Schema(description = "유저 ID", example = "5")
    private Long userId;

    @Schema(description = "즐겨찾기 생성일자", example = "2025-07-11T14:00:00")
    private LocalDateTime createdAt;
} 