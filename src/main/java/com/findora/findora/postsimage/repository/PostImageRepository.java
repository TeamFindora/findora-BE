package com.findora.findora.postsimage.repository;

import com.findora.findora.postsimage.model.PostImage;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;

@Tag(name = "PostImage Repository", description = "게시글 이미지 데이터 접근 계층")
public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    /**
     * 게시글 ID로 이미지 개수 조회
     * @param postId 게시글 ID
     * @return 이미지 개수
     */
    int countByPostId(Long postId);
    
    /**
     * 게시글 ID로 이미지 목록 조회
     * @param postId 게시글 ID
     * @return 이미지 목록
     */
    List<PostImage> findByPostId(Long postId);
    
    /**
     * 이미지 ID와 게시글 ID로 이미지 조회
     * @param imageId 이미지 ID
     * @param postId 게시글 ID
     * @return 이미지 (Optional)
     */
    Optional<PostImage> findByIdAndPostId(Long imageId, Long postId);
}
