package com.findora.findora.posts.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.findora.findora.posts.dto.PostRequestDto;
import com.findora.findora.posts.dto.PostResponseDto;
import com.findora.findora.posts.service.PostService;
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

/**
 * 게시판 API 컨트롤러
 * 
 * 권한별 접근 정책:
 * - 비로그인: 모든 게시글 조회 가능 (댓글 제외)
 * - 로그인: 학생/교수 권한에 따라 다른 범위의 게시글 작성 가능
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@Tag(name = "📝 게시판 API", description = "게시글 CRUD 및 카테고리별 조회 API")
public class PostController {
    private final PostService postService;

    @PostMapping
    @Operation(
        summary = "게시글 작성", 
        description = "새로운 게시글을 작성합니다. 제목, 내용, 카테고리 ID가 필요합니다.",
        security = @SecurityRequirement(name = "Authorization")
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "게시글 작성 정보",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = PostRequestDto.class),
            examples = @ExampleObject(
                name = "게시글 작성 예시",
                value = """
                {
                  \"title\": \"Spring Boot 질문드립니다\",
                  \"content\": \"Spring Boot에서 JPA 연관관계 매핑에 대해 질문이 있습니다. OneToMany 관계에서 N+1 문제를 어떻게 해결하는지 알고 싶습니다.\",
                  \"categoryId\": 1
                }
                """
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "게시글 작성 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = "15"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청 (필수 필드 누락, 유효하지 않은 카테고리 ID)",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                      \"error\": \"제목을 필수로 입력해주세요.\",
                      \"timestamp\": \"2025-07-07T14:30:00\"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "인증 필요 (로그인되지 않은 사용자)"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "권한 부족 (해당 카테고리에 글 작성 권한 없음)"
        )
    })
    public ResponseEntity<Long> create(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody PostRequestDto dto) {
        return ResponseEntity.ok(postService.createPost(dto, user.getId()));
    }

    @GetMapping
    @Operation(
        summary = "전체 게시글 조회", 
        description = "모든 카테고리의 게시글을 최신순으로 조회합니다. 비로그인 사용자도 접근 가능합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "전체 게시글 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PostResponseDto.class),
                examples = @ExampleObject(
                    name = "게시글 목록 응답",
                    value = """
                     [
                       {
                         "id": 15,
                         "category": {
                           "id": 1,
                           "name": "GENERAL",
                           "visibility": "PUBLIC",
                           "createdAt": "2025-07-07T10:00:00"
                         },
                         "userId": 1,
                         "userNickname": "김학생",
                         "title": "Spring Boot 질문드립니다",
                         "content": "Spring Boot에서 JPA 연관관계 매핑에 대해 질문이 있습니다.",
                         "createdAt": "2025-07-07T14:30:00",
                         "updatedAt": "2025-07-07T14:30:00"
                       },
                       {
                         "id": 14,
                         "category": {
                           "id": 2,
                           "name": "NOTICE",
                           "visibility": "PUBLIC",
                           "createdAt": "2025-07-07T10:00:00"
                         },
                         "userId": 2,
                         "userNickname": "박교수",
                         "title": "학과 공지사항",
                         "content": "2025년 1학기 수강신청 안내",
                         "createdAt": "2025-07-07T09:00:00",
                         "updatedAt": "2025-07-07T09:00:00"
                       }
                     ]
                     """
                )
            )
        )
    })
    public ResponseEntity<List<PostResponseDto>> getAll() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "게시글 상세 조회", 
        description = "게시글 ID로 특정 게시글의 상세 정보를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "게시글 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PostResponseDto.class),
                examples = @ExampleObject(
                    name = "게시글 상세 응답",
                    value = """
                     {
                       "id": 15,
                       "category": {
                         "id": 1,
                         "name": "GENERAL",
                         "visibility": "PUBLIC",
                         "createdAt": "2025-07-07T10:00:00"
                       },
                       "userId": 1,
                       "userNickname": "김학생",
                       "title": "Spring Boot 질문드립니다",
                       "content": "Spring Boot에서 JPA 연관관계 매핑에 대해 질문이 있습니다. OneToMany 관계에서 N+1 문제를 어떻게 해결하는지 알고 싶습니다.",
                       "createdAt": "2025-07-07T14:30:00",
                       "updatedAt": "2025-07-07T14:30:00"
                     }
                     """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "게시글을 찾을 수 없음",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                      "error": "게시글을 찾을 수 없습니다.",
                      "timestamp": "2025-07-07T14:30:00"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<PostResponseDto> getById(
        @Parameter(description = "조회할 게시글 ID", example = "15", required = true)
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(
        summary = "카테고리별 게시글 조회", 
        description = "특정 카테고리에 속한 게시글들을 최신순으로 조회합니다. 카테고리의 공개 범위에 따라 접근이 제한될 수 있습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "카테고리별 게시글 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PostResponseDto.class),
                examples = @ExampleObject(
                    name = "카테고리별 게시글 목록",
                    value = """
                     [
                       {
                         "id": 15,
                         "category": {
                           "id": 1,
                           "name": "GENERAL",
                           "visibility": "PUBLIC",
                           "createdAt": "2025-07-07T10:00:00"
                         },
                         "userId": 1,
                         "userNickname": "김학생",
                         "title": "Spring Boot 질문드립니다",
                         "content": "Spring Boot에서 JPA 연관관계 매핑에 대해 질문이 있습니다.",
                         "createdAt": "2025-07-07T14:30:00",
                         "updatedAt": "2025-07-07T14:30:00"
                       },
                       {
                         "id": 13,
                         "category": {
                           "id": 1,
                           "name": "GENERAL",
                           "visibility": "PUBLIC",
                           "createdAt": "2025-07-07T10:00:00"
                         },
                         "userId": 3,
                         "userNickname": "이개발",
                         "title": "React 질문",
                         "content": "React Hook 사용법에 대해 궁금합니다.",
                         "createdAt": "2025-07-07T13:00:00",
                         "updatedAt": "2025-07-07T13:00:00"
                       }
                     ]
                     """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "카테고리를 찾을 수 없음"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "해당 카테고리에 접근 권한 없음 (비공개 카테고리)"
        )
    })
    public ResponseEntity<List<PostResponseDto>> getPostsByCategory(
        @Parameter(
            description = "조회할 카테고리 ID (1: 일반, 2: 공지사항, 3: 질문&답변)", 
            example = "1", 
            required = true
        )
        @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(postService.getPostsByCategory(categoryId));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "게시글 수정", 
        description = "기존 게시글의 내용을 수정합니다. 작성자 본인만 수정 가능합니다. (카테고리는 수정 불가)",
        security = @SecurityRequirement(name = "Authorization")
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "수정할 게시글 정보 (카테고리는 수정 불가, 제목/내용만 입력)",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = PostRequestDto.class),
            examples = @ExampleObject(
                name = "게시글 수정 예시",
                value = """
                {
                  \"title\": \"Spring Boot 질문드립니다 (수정됨)\",
                  \"content\": \"Spring Boot에서 JPA 연관관계 매핑에 대해 질문이 있습니다. OneToMany 관계에서 N+1 문제를 Fetch Join으로 해결할 수 있다는 답변을 받았습니다. 감사합니다!\"
                }
                """
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", 
            description = "게시글 수정 성공"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청 (필수 필드 누락)"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "인증 필요"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "권한 부족 (작성자가 아님)"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "게시글을 찾을 수 없음"
        )
    })
    public ResponseEntity<Void> update(
        @AuthenticationPrincipal CustomUserDetails user,
        @Parameter(description = "수정할 게시글 ID", example = "15", required = true)
        @PathVariable Long id, 
        @RequestBody PostRequestDto dto
    ) {
        postService.updatePost(id, dto, user.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "게시글 삭제", 
        description = "게시글을 삭제합니다. 작성자 본인만 삭제 가능합니다.",
        security = @SecurityRequirement(name = "Authorization")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", 
            description = "게시글 삭제 성공"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "인증 필요"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "권한 부족 (작성자가 아님)"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "게시글을 찾을 수 없음"
        )
    })
    public ResponseEntity<Void> delete(
        @AuthenticationPrincipal CustomUserDetails user,
        @Parameter(description = "삭제할 게시글 ID", example = "15", required = true)
        @PathVariable Long id
    ) {
        postService.deletePost(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
