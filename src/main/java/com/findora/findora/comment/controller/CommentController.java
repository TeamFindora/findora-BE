package com.findora.findora.comment.controller;

import com.findora.findora.comment.dto.CommentRequestDto;
import com.findora.findora.comment.dto.CommentResponseDto;
import com.findora.findora.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comment", description = "댓글 API")
@RestController
@RequestMapping("api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 작성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "게시글 또는 부모 댓글 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<Long> createComment(@PathVariable Long postId,@Valid @RequestBody CommentRequestDto dto) {
        //dto.setPostId(postId);
        return ResponseEntity.ok(commentService.createComment(dto));
    }

    @Operation(summary = "게시글 댓글 목록 조회", description = "게시글에 달린 모든 댓글을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 조회 성공")
    })
    @GetMapping
    public ResponseEntity<List<CommentResponseDto>> getCommentsByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }


    @Operation(summary = "댓글 수정", description = "기존 댓글의 내용을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateComment(@PathVariable Long postId,@PathVariable Long id, @RequestBody CommentRequestDto dto) {
        commentService.updateComment(id, dto.getContent());
        return ResponseEntity.ok().build();
    }



    @Operation(summary = "댓글 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long postId,@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok().build();
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
