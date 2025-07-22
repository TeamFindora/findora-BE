package com.findora.findora.likes.controller;

import com.findora.findora.likes.dto.LikeResponseDto;
import com.findora.findora.likes.service.LikeService;
import com.findora.findora.users.service.CustomUserDetails;
import com.findora.findora.common.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Tag(name = "Like", description = "좋아요 API")
@RestController
@RequestMapping("api/posts/{postId}/likes")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    // user의 게시글 좋아요 상태
    @GetMapping("/status")
    @Operation(summary = "게시글 user의 좋아요 상태 ")
    public ResponseEntity<LikeResponseDto> getPostLikeStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) Long userId) {
        LikeResponseDto response = likeService.getPostLikeStatus(postId, user.getId());
            
        return ResponseEntity.ok(response);
    }

    // 게시글 좋아요 누르기/취소
    @PostMapping("/toggle")
    @Operation(summary = "게시글 좋아요 누르기/취소")
    public ResponseEntity<SuccessResponse> togglePostLike(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long postId) {
        SuccessResponse response = likeService.togglePostLike(user.getId(), postId);
        return ResponseEntity.ok(response);
    }

    // user의 댓글 좋아요 상태
    @GetMapping("/comments/{commentId}/status")
    @Operation(summary = "댓글 user의 좋아요 상태")
    public ResponseEntity<LikeResponseDto> getCommentLikeStatus(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails user) {
        LikeResponseDto response = likeService.getCommentLikeStatus(commentId, user.getId());
        return ResponseEntity.ok(response);
    }

    // 댓글 좋아요 좋아요 누르기/취소
    @PostMapping("/comments/{commentId}/toggle")
    @Operation(summary = "댓글 좋아요 누르기/취소")
    public ResponseEntity<SuccessResponse> toggleCommentLike(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        SuccessResponse response = likeService.toggleCommentLike(user.getId(), commentId);
        return ResponseEntity.ok(response);
    }

    // 게시글 좋아요 수 조회
    @GetMapping("/count")
    @Operation(summary = "게시글 좋아요 수 조회")
    public ResponseEntity<Map<String, Long>> countPostLikes(@PathVariable Long postId) {
        long count = likeService.countPostLikes(postId);
        System.out.println("count: " + count);
        return ResponseEntity.ok(Map.of("postLikeCount", count));
    }

    // 댓글 좋아요 수 조회
    @GetMapping("/comments/{commentId}/count")
    @Operation(summary = "댓글 좋아요 수 조회")
    public ResponseEntity<Map<String, Long>> countCommentLikes(@PathVariable Long postId,@PathVariable Long commentId) {
        long count = likeService.countCommentLikes(commentId);
        return ResponseEntity.ok(Map.of("commentLikeCount", count));
    }
}