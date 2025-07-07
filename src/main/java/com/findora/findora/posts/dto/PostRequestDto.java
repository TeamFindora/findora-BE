package com.findora.findora.posts.dto;

import com.findora.findora.posts.model.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDto {
    @Schema(description = "게시글 제목", example = "제목을 입력하세요.")
    private String title;

    @Schema(description = "게시글 본문", example = "내용을 입력하세요.")
    private String content;

    public Post toEntity() {
        return Post.builder()
                .title(title)
                .content(content)
                .build();
    }

    /*카테고리id, 사용자id 추가예정 +첨부파일
    @Schema(description = "카테고리 ID")
    private Long categoryId;

    @Schema(description = "작성자 ID")
    private Long userId;*/
}
