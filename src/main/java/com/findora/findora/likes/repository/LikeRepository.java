package com.findora.findora.likes.repository;

import com.findora.findora.comment.model.Comment;
import com.findora.findora.likes.model.Like;
import com.findora.findora.posts.model.Post;
import com.findora.findora.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);
    Optional<Like> findByUserIdAndCommentId(Long userId, Long commentId);
    long countByPostId(Long postId);
    long countByCommentId(Long commentId);

}