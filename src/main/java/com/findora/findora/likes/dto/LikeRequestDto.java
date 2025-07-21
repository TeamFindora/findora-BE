package com.findora.findora.likes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LikeRequestDto {
    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "댓글 ID", example = "1")
    private Long commentId;
}