package com.findora.findora.posts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PostResponseDto {
    @Schema(description = "게시글 ID")
    private Long id;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "본문")
    private String content;

    @Schema(description = "작성일시")
    private LocalDateTime createdAt;

    /*userid, categoryid
    private Long categoryid;
    private Long userid;
     */
}
