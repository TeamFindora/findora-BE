package com.findora.findora.comment.dto;

import com.findora.findora.comment.model.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDto {
    @Schema(description = "댓글 ID")
    private Long id;

    @Schema(description = "부모 댓글 ID(대댓글인 경우)")
    private Long parentId;

    @Schema(description = "게시글 ID")
    private Long postId;

    @Schema(description = "작성자 ID")
    private Long userId;

    @Schema(description = "작성자 닉네임")
    private String userNickname;

    @Schema(description = "댓글 내용")
    private String content;

    @Schema(description = "작성일자")
    private LocalDateTime createdAt;

    @Schema(description = "수정일자")
    private LocalDateTime updatedAt;

    @Schema(description = "삭제 여부", example = "false")
    private boolean isDeleted;


    // Entity → DTO 변환 메서드
    public static CommentResponseDto fromEntity(Comment comment) {
        // 댓글이 삭제되었거나 작성자가 삭제된 경우 확인
        boolean isCommentDeleted = comment.isDeleted();
        boolean isUserDeleted = comment.getUser() == null || comment.getUser().isDeleted();
        boolean shouldHideUserInfo = isCommentDeleted || isUserDeleted;
        
        // 사용자 정보 안전하게 가져오기
        Long userId = null;
        String userNickname = "삭제된 사용자";
        
        if (!shouldHideUserInfo && comment.getUser() != null) {
            userId = comment.getUser().getId();
            userNickname = comment.getUser().getNickname();
        }
        
        return CommentResponseDto.builder()
                .id(comment.getId())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .postId(comment.getPost().getId())
                .userId(userId)
                .userNickname(userNickname)
                .content(isCommentDeleted ? "(삭제된 댓글입니다)" : comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isDeleted(comment.isDeleted())
                .build();
    }
}


