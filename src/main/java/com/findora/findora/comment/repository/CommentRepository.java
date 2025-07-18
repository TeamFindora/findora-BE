package com.findora.findora.comment.repository;

import com.findora.findora.comment.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 삭제되지 않은 댓글만 불러오는 메서드 추가
    List<Comment> findByPostIdAndIsDeletedFalse(Long postId);
    List<Comment> findByParentIdAndIsDeletedFalse(Long parentId);

}
