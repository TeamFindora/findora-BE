package com.findora.findora.likes.repository;

import com.findora.findora.comment.model.Comment;
import com.findora.findora.likes.model.Like;
import com.findora.findora.posts.model.Post;
import com.findora.findora.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    // 게시글 좋아요 관련 (comment_id가 null)
    @Query("SELECT l FROM Like l WHERE l.user.id = :userId AND l.post.id = :postId AND l.comment IS NULL")
    Optional<Like> findByUserIdAndPostIdAndCommentIsNull(@Param("userId") Long userId, @Param("postId") Long postId);
    
    @Query("SELECT COUNT(l) FROM Like l WHERE l.post.id = :postId AND l.comment IS NULL")
    long countByPostIdAndCommentIsNull(@Param("postId") Long postId);
    
    // 댓글 좋아요 관련 (comment_id가 null이 아님)
    @Query("SELECT l FROM Like l WHERE l.user.id = :userId AND l.comment.id = :commentId AND l.comment IS NOT NULL")
    Optional<Like> findByUserIdAndCommentIdAndCommentIsNotNull(@Param("userId") Long userId, @Param("commentId") Long commentId);
    
    @Query("SELECT COUNT(l) FROM Like l WHERE l.comment.id = :commentId AND l.comment IS NOT NULL")
    long countByCommentIdAndCommentIsNotNull(@Param("commentId") Long commentId);
    
    // 기존 메서드들 (하위 호환성을 위해 유지)
    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);
    Optional<Like> findByUserIdAndCommentId(Long userId, Long commentId);
    long countByPostId(Long postId);
    long countByCommentId(Long commentId);
}