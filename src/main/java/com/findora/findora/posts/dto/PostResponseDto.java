package com.findora.findora.posts.dto;

import com.findora.findora.posts.model.Post;
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
public class PostResponseDto {
    @Schema(description = "게시글 ID")
    private Long id;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "본문")
    private String content;

    @Schema(description = "작성일자")
    private LocalDateTime createdAt;

    @Schema(description = "수정일자")
    private LocalDateTime updatedAt;


    // Entity → DTO 변환 메서드
    public static PostResponseDto fromEntity(Post post) {
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
    /*userid, categoryid
    private Long categoryid;
    private Long userid;
     */
}
