package com.findora.findora.posts.controller;

import com.findora.findora.posts.dto.PostRequestDto;
import com.findora.findora.posts.dto.PostResponseDto;
import com.findora.findora.posts.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@Tag(name = "Post API", description = "게시판 CRUD API")
public class PostController {
    private final PostService postService;

    @PostMapping
    @Operation(summary = "게시글 작성", description = "제목과 내용을 입력받아 새 게시글을 생성합니다.")
    public ResponseEntity<Long> createPost(@RequestBody PostRequestDto requestDto) {
        Long id = postService.createPost(requestDto);
        return ResponseEntity.ok(id);
    }

    @GetMapping
    @Operation(summary = "게시글 전체 조회", description = "등록된 모든 게시글을 조회합니다.")
    public ResponseEntity<List<PostResponseDto>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID로 게시글 조회", description = "ID를 이용해 특정 게시글의 상세 정보를 조회합니다.")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "게시글 수정", description = "ID를 이용해 게시글의 제목과 내용을 수정합니다.")
    public ResponseEntity<Void> updatePost(@PathVariable Long id, @RequestBody PostRequestDto requestDto) {
        postService.updatePost(id, requestDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "게시글 삭제", description = "ID를 이용해 게시글을 삭제합니다.")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
