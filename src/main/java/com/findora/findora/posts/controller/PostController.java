package com.findora.findora.posts.controller;

import com.findora.findora.posts.dto.PostRequestDto;
import com.findora.findora.posts.dto.PostResponseDto;
import com.findora.findora.posts.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*로그인X-> 모든 게시판 조회 가능(댓글 X),
* 로그인O-> 학생, 교수 범위를 다르게 하여 게시글 작성*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@Tag(name = "Post API", description = "게시판 CRUD API")
public class PostController {
    private final PostService postService;

    @PostMapping
    @Operation(summary = "게시글 작성", responses = {
            @ApiResponse(responseCode = "200", description = "작성 성공",
                    content = @Content(schema = @Schema(implementation = PostResponseDto.class)))
    })
    public ResponseEntity<Long> create(@RequestBody PostRequestDto dto) {
        return ResponseEntity.ok(postService.createPost(dto));
    }

    @GetMapping
    @Operation(summary = "전체 게시글 조회", responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PostResponseDto.class)))
    })
    public ResponseEntity<List<PostResponseDto>> getAll() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "게시글 ID로 조회", responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PostResponseDto.class)))
    })
    public ResponseEntity<PostResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "게시글 수정", responses = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = PostResponseDto.class)))
    })
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody PostRequestDto dto) {
        postService.updatePost(id, dto);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "게시글 삭제", responses = {
            @ApiResponse(responseCode = "204", description = "삭제 성공")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
