package com.findora.findora.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateRequestDto {
    @Schema(description = "댓글 내용", example = "수정된 댓글 내용입니다", required = true)
    private String content;
} 