package com.findora.findora.likes.controller;

import com.findora.findora.likes.dto.LikeResponseDto;
import com.findora.findora.likes.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Like", description = "좋아요 API")
@RestController
@RequestMapping("api/posts/{postId}/likes")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    // 게시글 좋아요 상태 조회
    @GetMapping("/status")
    @Operation(summary = "게시글 좋아요 상태 조회")
    public ResponseEntity<LikeResponseDto> getPostLikeStatus(
            @PathVariable Long postId,
            @RequestParam(required = false) Long userId) {
        LikeResponseDto response = likeService.getPostLikeStatus(postId, userId);
        return ResponseEntity.ok(response);
    }

    // 게시글 좋아요 토글
    @PostMapping("/toggle")
    @Operation(summary = "게시글 좋아요 토글 (좋아요 누르기/취소)")
    public ResponseEntity<LikeResponseDto> togglePostLike(
            @RequestParam Long userId,
            @PathVariable Long postId) {
        LikeResponseDto response = likeService.togglePostLike(userId, postId);
        return ResponseEntity.ok(response);
    }

    // 댓글 좋아요 상태 조회
    @GetMapping("/comments/{commentId}/status")
    @Operation(summary = "댓글 좋아요 상태 조회")
    public ResponseEntity<LikeResponseDto> getCommentLikeStatus(
            @PathVariable Long commentId,
            @RequestParam(required = false) Long userId) {
        LikeResponseDto response = likeService.getCommentLikeStatus(commentId, userId);
        return ResponseEntity.ok(response);
    }

    // 댓글 좋아요 토글
    @PostMapping("/comments/{commentId}/toggle")
    @Operation(summary = "댓글 좋아요 토글 (좋아요 누르기/취소)")
    public ResponseEntity<LikeResponseDto> toggleCommentLike(
            @RequestParam Long userId,
            @PathVariable Long commentId) {
        LikeResponseDto response = likeService.toggleCommentLike(userId, commentId);
        return ResponseEntity.ok(response);
    }

    // 게시글 좋아요 수 조회
    @GetMapping("/count")
    @Operation(summary = "게시글 좋아요 수 조회")
    public ResponseEntity<Long> countPostLikes(@PathVariable Long postId) {
        long count = likeService.countPostLikes(postId);
        return ResponseEntity.ok(count);
    }

    // 댓글 좋아요 수 조회
    @GetMapping("/comments/{commentId}/count")
    @Operation(summary = "댓글 좋아요 수 조회")
    public ResponseEntity<Long> countCommentLikes(@PathVariable Long postId,@PathVariable Long commentId) {
        long count = likeService.countCommentLikes(commentId);
        return ResponseEntity.ok(count);
    }
}