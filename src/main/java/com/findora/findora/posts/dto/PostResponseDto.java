package com.findora.findora.posts.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.findora.findora.categories.dto.CategoryResponseDto;
import com.findora.findora.posts.model.Post;

import com.findora.findora.postsimage.model.PostImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponseDto {
    @Schema(description = "게시글 ID")
    private Long id;

    @Schema(description = "카테고리 정보")
    private CategoryResponseDto category;

    @Schema(description = "작성자 ID")
    private Long userId;

    @Schema(description = "작성자 닉네임")
    private String userNickname;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "본문")
    private String content;

    @Schema(description = "조회수")
    private Long viewCount;

    @Schema(description = "작성일자")
    private LocalDateTime createdAt;

    @Schema(description = "수정일자")
    private LocalDateTime updatedAt;

    @Schema(description = "이미지 URL 목록")
    private List<String> imageUrls;



    // Entity → DTO 변환 메서드
    public static PostResponseDto fromEntity(Post post, boolean isDetailView) {
        // 사용자 정보 안전하게 가져오기
        Long userId = null;
        String userNickname = "탈퇴한 사용자";

        List<String> images = post.getImages().stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());

        try {
            if (post.getUser() != null && !post.getUser().isDeleted()) {
                userId = post.getUser().getId();
                userNickname = post.getUser().getNickname();
            }
        } catch (Exception e) {
            // User 로딩 실패 시 기본값 유지
            userId = null;
            userNickname = "탈퇴한 사용자";
        }
        
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .imageUrls(isDetailView ? images : // 상세 조회시 모든 이미지
                        (images.isEmpty() ? null : List.of(images.get(0)))) // 목록 조회시 첫번째 이미지만
                .category(CategoryResponseDto.fromEntity(post.getCategory()))
                .userId(userId)
                .userNickname(userNickname)
                .build();
    }
}
