package com.findora.findora.likes.service;

import com.findora.findora.comment.model.Comment;
import com.findora.findora.comment.repository.CommentRepository;
import com.findora.findora.likes.dto.LikeStatusResponseDto;
import com.findora.findora.likes.dto.LikeResponseDto;
import com.findora.findora.common.SuccessResponse;
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
    public SuccessResponse togglePostLike(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 게시글 좋아요 조회 (comment_id가 null인 경우만)
        Optional<Like> existingLike = likeRepository.findByUserIdAndPostIdAndCommentIsNull(userId, postId);
        boolean liked;
        String message;

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            liked = false;
            message = "게시글 좋아요가 취소되었습니다.";
        } else {
            Like like = Like.builder()
                    .user(user)
                    .post(post)
                    // comment는 null로 설정 (게시글 좋아요)
                    .build();
            likeRepository.save(like);
            liked = true;
            message = "게시글 좋아요가 추가되었습니다.";
        }

        // 게시글 좋아요 개수 조회 (comment_id가 null인 경우만)
        long likeCount = likeRepository.countByPostIdAndCommentIsNull(postId);
        return SuccessResponse.of(message);
    }

    @Transactional
    public SuccessResponse toggleCommentLike(Long userId, Long commentId) {
       
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        // 댓글 좋아요 조회 (comment_id가 null이 아닌 경우만)
        Optional<Like> existingLike = likeRepository.findByUserIdAndCommentIdAndCommentIsNotNull(userId, commentId);
        boolean liked;
        String message;

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            liked = false;
            message = "댓글 좋아요가 취소되었습니다.";
        } else {
            Like like = Like.builder()
                    .user(user)
                    .comment(comment)
                    .post(comment.getPost())  // 댓글의 post 정보 추가
                    .build();
            likeRepository.save(like);
            liked = true;
            message = "댓글 좋아요가 추가되었습니다.";
        }

        // 댓글 좋아요 개수 조회 (comment_id가 null이 아닌 경우만)
        long likeCount = likeRepository.countByCommentIdAndCommentIsNotNull(commentId);
        
        return SuccessResponse.of(message);
    }

    //본인이 게시글에 좋아요를 눌렀는지 (comment_id가 null인 경우만)
    public LikeStatusResponseDto getPostLikeStatus(Long postId, Long userId) {
        // 게시글 좋아요만 카운트 (comment_id가 null인 경우)
        long likeCount = likeRepository.countByPostIdAndCommentIsNull(postId);
        boolean isLiked = false;
        
        if (userId != null) {
            // 해당 사용자의 게시글 좋아요 상태 확인 (comment_id가 null인 경우만)
            isLiked = likeRepository.findByUserIdAndPostIdAndCommentIsNull(userId, postId).isPresent();
        }
         
        return LikeStatusResponseDto.forPost(isLiked, likeCount);
    }

    //본인이 댓글에 좋아요를 눌렀는지 (comment_id가 null이 아닌 경우만)
    public LikeStatusResponseDto getCommentLikeStatus(Long commentId, Long userId) {
        // 댓글 좋아요만 카운트 (comment_id가 null이 아닌 경우)
        long likeCount = likeRepository.countByCommentIdAndCommentIsNotNull(commentId);
        boolean isLiked = false;
        
        if (userId != null) {
            // 해당 사용자의 댓글 좋아요 상태 확인 (comment_id가 null이 아닌 경우만)
            isLiked = likeRepository.findByUserIdAndCommentIdAndCommentIsNotNull(userId, commentId).isPresent();
        }
        
       
        return LikeStatusResponseDto.forComment(isLiked, likeCount);
    }

    @Transactional(readOnly = true)
    public long countPostLikes(Long postId) {
        // 게시글 좋아요만 카운트 (comment_id가 null인 경우)
        return likeRepository.countByPostIdAndCommentIsNull(postId);
    }

    @Transactional(readOnly = true)
    public long countCommentLikes(Long commentId) {
        // 댓글 좋아요만 카운트 (comment_id가 null이 아닌 경우)
        return likeRepository.countByCommentIdAndCommentIsNotNull(commentId);
    }
}
