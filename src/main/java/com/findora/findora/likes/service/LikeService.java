package com.findora.findora.likes.service;

import com.findora.findora.comment.model.Comment;
import com.findora.findora.comment.repository.CommentRepository;
import com.findora.findora.likes.dto.LikeResponseDto;
import com.findora.findora.likes.model.Like;
import com.findora.findora.likes.repository.LikeRepository;
import com.findora.findora.posts.model.Post;
import com.findora.findora.posts.repository.PostRepository;
import com.findora.findora.users.model.User;
import com.findora.findora.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public LikeResponseDto togglePostLike(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        Optional<Like> existingLike = likeRepository.findByUserIdAndPostId(userId, postId);
        boolean liked;

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            liked = false;
        } else {
            Like like = Like.builder()
                    .user(user)
                    .post(post)
                    .build();
            likeRepository.save(like);
            liked = true;
        }

        long likeCount = likeRepository.countByPostId(postId);
        return LikeResponseDto.builder()
                .likeCount(likeCount)
                .liked(liked)
                .build();
    }

    @Transactional
    public LikeResponseDto toggleCommentLike(Long userId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        Optional<Like> existingLike = likeRepository.findByUserIdAndCommentId(userId, commentId);
        boolean liked;

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            liked = false;
        } else {
            Like like = Like.builder()
                    .user(user)
                    .comment(comment)
                    .build();
            likeRepository.save(like);
            liked = true;
        }

        long likeCount = likeRepository.countByCommentId(commentId);
        return LikeResponseDto.builder()
                .likeCount(likeCount)
                .liked(liked)
                .build();
    }

    //본인이 게시글에 좋아요를 눌렀는지
    public LikeResponseDto getPostLikeStatus(Long postId, Long userId) {
        long likeCount = likeRepository.countByPostId(postId);
        if (userId == null) {
            return LikeResponseDto.builder()
                    .likeCount(likeCount)
                    .liked(false)
                    .build();
        }
        boolean isLiked = likeRepository.findByUserIdAndPostId(userId, postId).isPresent();
        return LikeResponseDto.builder()
                .likeCount(likeCount)
                .liked(isLiked)
                .build();
    }

    //본인이 댓글에 좋아요를 눌렀는지
    public LikeResponseDto getCommentLikeStatus(Long commentId, Long userId) {
        long likeCount = likeRepository.countByCommentId(commentId);
        if (userId == null) {
            return LikeResponseDto.builder()
                    .likeCount(likeCount)
                    .liked(false)
                    .build();
        }
        boolean isLiked = likeRepository.findByUserIdAndCommentId(userId, commentId).isPresent();
        return LikeResponseDto.builder()
                .likeCount(likeCount)
                .liked(isLiked)
                .build();
    }

    @Transactional(readOnly = true)
    public long countPostLikes(Long postId) {
        return likeRepository.countByPostId(postId);
    }

    @Transactional(readOnly = true)
    public long countCommentLikes(Long commentId) {
        return likeRepository.countByCommentId(commentId);
    }
}
