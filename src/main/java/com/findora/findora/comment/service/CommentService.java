package com.findora.findora.comment.service;

import com.findora.findora.comment.dto.CommentRequestDto;
import com.findora.findora.comment.dto.CommentUpdateRequestDto;
import com.findora.findora.comment.dto.CommentResponseDto;
import com.findora.findora.comment.model.Comment;
import com.findora.findora.comment.repository.CommentRepository;
import com.findora.findora.common.SuccessResponse;
import com.findora.findora.posts.dto.PostResponseDto;
import com.findora.findora.posts.model.Post;
import com.findora.findora.posts.repository.PostRepository;
import com.findora.findora.users.model.User;
import com.findora.findora.users.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.*;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;


    //댓글 등록
    @Transactional
    public SuccessResponse createComment(Long postId, Long userId, CommentRequestDto dto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다. id=" + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자가 존재하지 않습니다. id=" + userId));

        Comment parent = null;
        if (dto.getParentId() != null) {
            parent = commentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("부모 댓글이 존재하지 않습니다. id=" + dto.getParentId()));
        }

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .parent(parent)
                .content(dto.getContent())
                .build();

        commentRepository.save(comment);
        
        return SuccessResponse.of("댓글이 성공적으로 등록되었습니다.");
    }

    //게시글 ID로 댓글 조회(삭제된 댓글도 포함, 삭제된 댓글은 내용을 "삭제된 댓글입니다"로 표시)
    @Transactional(readOnly = true)
    public Object getCommentsByPostId(Long postId) {
        // 삭제된 댓글도 포함해서 조회 (네이티브 쿼리로 @Where 어노테이션 무시)
        List<Comment> comments = commentRepository.findByPostIdIncludingDeleted(postId);

        List<CommentResponseDto> commentDtos = comments.stream()
                .map(CommentResponseDto::fromEntity)
                .collect(Collectors.toList());
                
        if (commentDtos.isEmpty()) {
            return SuccessResponse.of("댓글이 없습니다");
        } else {
            return commentDtos;
        }
    }

    /*대댓글 조회
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getRepliesByParentId(Long parentId) {
        List<Comment> replies = commentRepository.findByParentIdAndIsDeletedFalse(parentId);

        return replies.stream()
                .map(CommentResponseDto::fromEntity)
                .collect(Collectors.toList());
    }*/

    //댓글 수정
    @Transactional
    public SuccessResponse updateComment(Long postId, Long commentId, Long userId, CommentUpdateRequestDto dto) {
        // 게시글 존재 여부 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다. id=" + postId));
        
        // @Where 어노테이션으로 자동 필터링되므로 isDeleted() 체크 불필요
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글이 존재하지 않습니다. id=" + commentId));
        
        // 댓글이 해당 게시글에 속하는지 확인
        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글에 속하지 않는 댓글입니다.");
        }
        
        // 댓글 작성자와 현재 사용자가 같은지 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new SecurityException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }
        
        comment.updateContent(dto.getContent());
        
        return SuccessResponse.of("댓글이 성공적으로 수정되었습니다.");
    }

    //댓글 삭제상태로 변경
    @Transactional
    public SuccessResponse deleteComment(Long postId, Long commentId, Long userId) {
        // 게시글 존재 여부 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다. id=" + postId));
        
        // @Where 어노테이션으로 자동 필터링되므로 isDeleted() 체크 불필요
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글이 존재하지 않습니다. id=" + commentId));

        // 댓글이 해당 게시글에 속하는지 확인
        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글에 속하지 않는 댓글입니다.");
        }

        // 댓글 작성자와 현재 사용자가 같은지 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new SecurityException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        comment.markAsDeleted(); // 삭제상태로 변경(실제 삭제 X)
        
        return SuccessResponse.of("댓글이 성공적으로 삭제되었습니다.");
    }

    //전체 댓글 조회(controller 수정중)
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getAllComments() {
        return commentRepository.findAll().stream()
                .map(CommentResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    //게시글 ID로 모든 댓글 소프트 삭제 (게시글 삭제 시 사용)
    @Transactional
    public void deleteAllCommentsByPostId(Long postId) {
        // 해당 게시글의 모든 댓글 조회 (삭제되지 않은 댓글만)
        List<Comment> comments = commentRepository.findByPostIdAndDeletedFalse(postId);
        
        // 모든 댓글을 소프트 삭제
        for (Comment comment : comments) {
            comment.markAsDeleted();
        }
    }
}
