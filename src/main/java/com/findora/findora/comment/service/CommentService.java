package com.findora.findora.comment.service;

import com.findora.findora.comment.dto.CommentRequestDto;
import com.findora.findora.comment.dto.CommentResponseDto;
import com.findora.findora.comment.model.Comment;
import com.findora.findora.comment.repository.CommentRepository;
import com.findora.findora.common.SuccessResponse;
import com.findora.findora.posts.dto.PostResponseDto;
import com.findora.findora.posts.model.Post;
import com.findora.findora.posts.repository.PostRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;


    //댓글 등록
    @Transactional
    public SuccessResponse createComment(Long postId, CommentRequestDto dto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. id=" + postId));

        Comment parent = null;
        if (dto.getParentId() != null) {
            parent = commentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다. id=" + dto.getParentId()));
        }

        Comment comment = Comment.builder()
                .post(post)
                .parent(parent)
                .content(dto.getContent())
                .isDeleted(false)
                .build();

        commentRepository.save(comment);
        
        return SuccessResponse.of("댓글이 성공적으로 등록되었습니다.");
    }

    //게시글 ID로 댓글 조회(논리 삭제된 댓글 제외)
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdAndIsDeletedFalse(postId);

        return comments.stream()
                .map(CommentResponseDto::fromEntity)
                .collect(Collectors.toList());
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
    public void updateComment(Long id, String newContent) {
        Comment comment = commentRepository.findById(id)
                .filter(c -> !c.getIsDeleted()) //삭제 상태인 댓글
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. id=" + id));
        comment.updateContent(newContent);
    }

    //댓글 삭제상태로 변경
    @Transactional
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. id=" + id));

        // 이미 삭제된 댓글인지 확인
        if (comment.getIsDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 댓글입니다. id=" + id);
        }

        comment.markAsDeleted(); // 삭제상태로 변경(실제 삭제 X)
    }

    //전체 댓글 조회(controller 수정중)
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getAllComments() {
        return commentRepository.findAll().stream()
                .map(CommentResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
