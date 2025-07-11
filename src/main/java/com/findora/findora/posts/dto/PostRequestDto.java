package com.findora.findora.posts.dto;

import com.findora.findora.categories.model.Category;
import com.findora.findora.posts.model.Post;
import com.findora.findora.users.model.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDto {
    @Schema(description = "게시글 제목", example = "제목을 입력하세요.")
    @NotBlank(message = "제목을 필수로 입력해주세요.")
    private String title;

    @Schema(description = "게시글 본문", example = "내용을 입력하세요.")
    @NotBlank(message = "본문 내용을 입력하세요")
    private String content;

    @Schema(description = "카테고리 ID", example = "1")
    @NotNull(message = "카테고리 ID를 입력하세요.")
    private Long categoryId;

    @Schema(description = "작성자 ID", example = "1")
    @NotNull(message = "작성자 ID를 입력하세요.")
    private Long userId;

    public Post toEntity(Category category, User user) {
        return Post.builder()
                .title(title)
                .content(content)
                .category(category)
                .user(user)
                .build();
    }

    /*사용자id 추가예정 +첨부파일
    @Schema(description = "작성자 ID")
    private Long userId;*/
}
