package com.findora.findora.postsimage.model;

import com.findora.findora.posts.model.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "게시글 이미지 엔티티")
public class PostImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "이미지 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @Schema(description = "연결된 게시글")
    private Post post;

    @Column(nullable = false)
    @Schema(description = "이미지 URL", example = "https://findora-images.s3.ap-northeast-2.amazonaws.com/posts/1/uuid-image.jpg")
    private String imageUrl;

    @Builder
    public PostImage(Post post, String imageUrl) {
        this.post = post;
        this.imageUrl = imageUrl;
    }
}
