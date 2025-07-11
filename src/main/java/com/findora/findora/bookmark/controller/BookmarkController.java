package com.findora.findora.bookmark.controller;

import com.findora.findora.bookmark.dto.BookmarkRequestDto;
import com.findora.findora.bookmark.dto.BookmarkResponseDto;
import com.findora.findora.bookmark.model.Bookmark;
import com.findora.findora.bookmark.service.BookmarkService;
import com.findora.findora.users.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bookmark API", description = "게시글 즐겨찾기 API")
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @PostMapping
    @Operation(
        summary = "게시글 즐겨찾기 추가",
        description = "게시글을 즐겨찾기에 추가합니다. (중복 불가)",
        security = @SecurityRequirement(name = "Authorization")
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "즐겨찾기 추가 요청",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BookmarkRequestDto.class),
            examples = @ExampleObject(
                value = """
                {
                  \"postId\": 10
                }
                """
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "즐겨찾기 추가 성공", content = @Content(schema = @Schema(implementation = Long.class), examples = @ExampleObject(value = "1"))),
        @ApiResponse(responseCode = "400", description = "이미 즐겨찾기한 게시글", content = @Content(examples = @ExampleObject(value = "{\"error\":\"이미 즐겨찾기한 게시글입니다.\"}"))),
        @ApiResponse(responseCode = "401", description = "인증 정보 없음", content = @Content(examples = @ExampleObject(value = "{\"error\":\"인증 정보가 없습니다.\"}")))
    })
    public ResponseEntity<Long> addBookmark(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody BookmarkRequestDto dto) {
        if (user == null) {
            log.error("[Bookmark] 인증 정보가 없습니다. principal=null");
            throw new RuntimeException("인증 정보가 없습니다.");
        }
        log.info("[Bookmark] 즐겨찾기 추가 요청 - userId: {}, loginId: {}, postId: {}", user.getId(), user.getLoginId(), dto.getPostId());
        Bookmark bookmark = bookmarkService.addBookmark(user.getId(), dto.getPostId());
        return ResponseEntity.ok(bookmark.getId());
    }

    @DeleteMapping
    @Operation(
        summary = "게시글 즐겨찾기 삭제",
        description = "즐겨찾기한 게시글을 삭제합니다.",
        security = @SecurityRequirement(name = "Authorization")
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "즐겨찾기 삭제 요청",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BookmarkRequestDto.class),
            examples = @ExampleObject(
                value = """
                {
                  \"postId\": 10
                }
                """
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "즐겨찾기 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "즐겨찾기 내역 없음", content = @Content(examples = @ExampleObject(value = "{\"error\":\"즐겨찾기 내역이 없습니다.\"}")))
    })
    public ResponseEntity<Void> removeBookmark(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody BookmarkRequestDto dto) {
        bookmarkService.removeBookmark(user.getId(), dto.getPostId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/me")
    @Operation(
        summary = "내 즐겨찾기 목록 조회",
        description = "내가 즐겨찾기한 게시글 목록을 조회합니다.",
        security = @SecurityRequirement(name = "Authorization")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "즐겨찾기 목록 조회 성공", content = @Content(schema = @Schema(implementation = BookmarkResponseDto.class), examples = @ExampleObject(value = """
        [
          {
            \"id\": 1,
            \"postId\": 10,
            \"postTitle\": \"Spring Boot 질문드립니다\",
            \"userId\": 1,
            \"createdAt\": \"2025-07-11T14:00:00\"
          }
        ]
        """)))
    })
    public ResponseEntity<List<BookmarkResponseDto>> getBookmarksByUser(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(bookmarkService.getBookmarksByUser(user.getId()));
    }
} 