package com.findora.findora.comment.controller;

import com.findora.findora.comment.dto.CommentRequestDto;
import com.findora.findora.comment.dto.CommentUpdateRequestDto;
import com.findora.findora.comment.dto.CommentResponseDto;

import com.findora.findora.comment.service.CommentService;
import com.findora.findora.common.SuccessResponse;
import com.findora.findora.users.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comment", description = "댓글 API")
@RestController
@RequestMapping("api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다. 로그인이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "게시글 또는 부모 댓글 찾을 수 없음")
    })
    @PostMapping
    @SecurityRequirement(name = "Authorization")
    public ResponseEntity<SuccessResponse> createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody CommentRequestDto dto) {
        return ResponseEntity.ok(commentService.createComment(postId, user.getId(), dto));
    }

    @Operation(summary = "게시글 댓글 목록 조회", description = "게시글에 달린 모든 댓글을 조회합니다. 댓글이 없으면 안내 메시지를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 조회 성공 또는 댓글 없음")
    })
    @GetMapping
    public ResponseEntity<Object> getCommentsByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }


    @Operation(summary = "댓글 수정", description = "기존 댓아글의 내용을 수정합니다. 본인이 작성한 댓글만 수정 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 댓글이 아님)"),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    @PutMapping("/{id}")
    @SecurityRequirement(name = "Authorization")
    public ResponseEntity<SuccessResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long id, 
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody CommentUpdateRequestDto dto) {
        return ResponseEntity.ok(commentService.updateComment(postId, id, user.getId(), dto));
    }



    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다. 본인이 작성한 댓글만 삭제 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (댓글이 해당 게시글에 속하지 않음)"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 댓글이 아님)"),
            @ApiResponse(responseCode = "404", description = "게시글 또는 댓글을 찾을 수 없음 (존재하지 않거나 이미 삭제됨)")
    })
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "Authorization")
    public ResponseEntity<SuccessResponse> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(commentService.deleteComment(postId, id, user.getId()));
    }

    /*전체댓글조회 계층구조 수정중
    @Operation(summary = "전체 댓글 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 댓글 조회 성공")
    })
    @GetMapping("/all")
    public ResponseEntity<List<CommentResponseDto>> getAllComments() {
        return ResponseEntity.ok(commentService.getAllComments());
    }*/

    /*@Operation(summary = "부모 댓글별 대댓글 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "대댓글 조회 성공")
    })
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<CommentResponseDto>> getRepliesByParentId(@PathVariable Long parentId) {
        return ResponseEntity.ok(commentService.getRepliesByParentId(parentId));
    }*/

}
