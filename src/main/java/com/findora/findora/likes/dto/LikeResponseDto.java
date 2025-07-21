package com.findora.findora.likes.dto;

import com.findora.findora.likes.model.Like;
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
public class LikeResponseDto {
    @Schema(description = "좋아요 ID")
    private Long id;

    @Schema(description = "사용자 ID")
    private Long userId;

    @Schema(description = "게시글 ID")
    private Long postId;

    @Schema(description = "댓글 ID")
    private Long commentId;

    @Schema(description = "좋아요 생성 시간")
    private LocalDateTime createdAt;

    @Schema(description = "좋아요 여부(true/false)")
    private boolean liked;      // 좋아요 눌렀는지 여부 (true/false)

    @Schema(description = "좋아요 총 개수")
    private Long likeCount;     // 좋아요 총 개수

    public static LikeResponseDto fromEntity(Like like, boolean liked, Long likeCount) {
        return LikeResponseDto.builder()
                .id(like.getId())
                .userId(like.getUser().getId())
                .postId(like.getPost() != null ? like.getPost().getId() : null)
                .commentId(like.getComment() != null ? like.getComment().getId() : null)
                .liked(liked)
                .likeCount(likeCount)
                .createdAt(like.getCreatedAt())
                .build();
    }
}