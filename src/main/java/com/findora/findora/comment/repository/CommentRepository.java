package com.findora.findora.comment.repository;

import com.findora.findora.comment.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 삭제되지 않은 댓글만 불러오는 메서드 (BaseEntity의 deleted 필드 사용)
    List<Comment> findByPostIdAndDeletedFalse(Long postId);
    List<Comment> findByParentIdAndDeletedFalse(Long parentId);

    // 삭제된 댓글도 포함해서 조회하는 메서드 (삭제된 User도 함께 조회)
    @Query(value = "SELECT c.* FROM comment c LEFT JOIN users u ON c.user_id = u.id WHERE c.post_id = :postId ORDER BY c.created_at ASC", nativeQuery = true)
    List<Comment> findByPostIdIncludingDeleted(@Param("postId") Long postId);

}
