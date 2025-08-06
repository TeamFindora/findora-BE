package com.findora.findora.postsimage.dto;

import com.findora.findora.postsimage.model.PostImage;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostImageResponseDto {
    // 게시글 이미지 응답 DTO
    private Long id;
    private String imageUrl;

    public static PostImageResponseDto fromEntity(PostImage image) {
        return PostImageResponseDto.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .build();

    }
}
