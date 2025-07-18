package com.findora.findora.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDto {
    @Schema(description = "부모 댓글 ID(대댓글일 경우 입력, 없으면 null)", example = "1", nullable = true)
    private Long parentId;

    @Schema(description = "댓글 내용", example = "댓글 작성합니다", required = true)
    private String content;
}
