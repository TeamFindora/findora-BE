package com.findora.findora.likes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LikeStatusResponseDto {
    
    @Schema(description = "게시글 좋아요 여부(true/false)")
    private Boolean postLiked;      // 게시글 좋아요 눌렀는지 여부

    @Schema(description = "댓글 좋아요 여부(true/false)")
    private Boolean commentLiked;   // 댓글 좋아요 눌렀는지 여부

    @Schema(description = "좋아요 총 개수")
    private Long likeCount;         // 좋아요 총 개수

    // 수동 setter 메서드 추가
    public void setPostLiked(Boolean postLiked) {
        this.postLiked = postLiked;
    }

    public void setCommentLiked(Boolean commentLiked) {
        this.commentLiked = commentLiked;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    // 게시글 좋아요 상태용 팩토리 메서드
    public static LikeStatusResponseDto forPost(boolean postLiked, long likeCount) {
        LikeStatusResponseDto dto = new LikeStatusResponseDto();
        dto.setPostLiked(postLiked);
        dto.setLikeCount(likeCount);
        return dto;
    }

    // 댓글 좋아요 상태용 팩토리 메서드
    public static LikeStatusResponseDto forComment(boolean commentLiked, long likeCount) {
        LikeStatusResponseDto dto = new LikeStatusResponseDto();
        dto.setCommentLiked(commentLiked);
        dto.setLikeCount(likeCount);
        return dto;
    }
}