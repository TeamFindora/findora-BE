package com.findora.findora.postsimage.dto;

import com.findora.findora.postsimage.model.PostImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "게시글 이미지 응답 DTO")
public class PostImageResponseDto {
    @Schema(description = "이미지 ID", example = "1")
    private Long id;
    
    @Schema(description = "이미지 URL", example = "https://findora-images.s3.ap-northeast-2.amazonaws.com/posts/1/uuid-image.jpg")
    private String imageUrl;

    public static PostImageResponseDto fromEntity(PostImage image) {
        return PostImageResponseDto.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .build();
    }
}
