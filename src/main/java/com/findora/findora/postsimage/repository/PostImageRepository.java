package com.findora.findora.postsimage.repository;

import com.findora.findora.postsimage.model.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.parameters.P;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    int countByPostId(Long postId); // 게시글 ID로 이미지 개수 조회
    List<PostImage> findByPostId(Long postId);
}
