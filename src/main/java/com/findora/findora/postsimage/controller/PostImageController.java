package com.findora.findora.postsimage.controller;

import com.findora.findora.common.SuccessResponse;
import com.findora.findora.postsimage.dto.PostImageResponseDto;
import com.findora.findora.postsimage.service.PostImageService;
import com.findora.findora.users.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/images")
@Tag(name = "🖼️ 게시글 이미지 API", description = "게시글 이미지 관리 API")
public class PostImageController {

    private final PostImageService postImageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "게시글 이미지 업로드",
        description = "특정 게시글에 이미지를 업로드합니다. 최대 10개까지 업로드 가능합니다. (게시글 작성자만 가능)"
    )
    @SecurityRequirement(name = "Authorization")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "이미지 업로드 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                      "message": "이미지 업로드 성공",
                      "timestamp": "2025-07-07T14:30:00"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (이미지 형식 오류, 개수 초과 등)"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 필요"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 부족 (게시글 작성자가 아님)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "게시글을 찾을 수 없음"
        )
    })
    public ResponseEntity<SuccessResponse> uploadImages(
        @AuthenticationPrincipal CustomUserDetails user,
        @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId,
        @Parameter(description = "업로드할 이미지 파일들 (최대 10개)") @RequestParam("images") List<MultipartFile> images
    ) {
        List<String> imageUrls = postImageService.savePostImages(postId, images, user.getId());
        return ResponseEntity.ok(SuccessResponse.of("이미지 업로드 성공"));
    }

    @GetMapping
    @Operation(
        summary = "게시글 이미지 목록 조회",
        description = "특정 게시글의 모든 이미지를 조회합니다. (모든 사용자 가능)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "이미지 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PostImageResponseDto.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "이미지 목록 응답",
                    value = """
                    [
                      {
                        "id": 1,
                        "imageUrl": "https://findora-images.s3.ap-northeast-2.amazonaws.com/posts/1/uuid-image1.jpg"
                      },
                      {
                        "id": 2,
                        "imageUrl": "https://findora-images.s3.ap-northeast-2.amazonaws.com/posts/1/uuid-image2.jpg"
                      }
                    ]
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "게시글을 찾을 수 없음"
        )
    })
    public ResponseEntity<List<PostImageResponseDto>> getImages(
        @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId
    ) {
        List<PostImageResponseDto> images = postImageService.getPostImages(postId);
        return ResponseEntity.ok(images);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "게시글 이미지 수정",
        description = "게시글의 이미지를 수정합니다. 기존 이미지를 삭제하고 새로운 이미지를 추가할 수 있습니다. (게시글 작성자만 가능)"
    )
    @SecurityRequirement(name = "Authorization")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "이미지 수정 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 필요"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 부족 (게시글 작성자가 아님)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "게시글을 찾을 수 없음"
        )
    })
    public ResponseEntity<SuccessResponse> updateImages(
        @AuthenticationPrincipal CustomUserDetails user,
        @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId,
        @Parameter(description = "새로 추가할 이미지 파일들") @RequestParam(value = "images", required = false) List<MultipartFile> newImages,
        @Parameter(description = "유지할 기존 이미지 ID 목록") @RequestParam(value = "remainImageIds", required = false) List<Long> remainImageIds
    ) {
        List<String> updatedImageUrls = postImageService.updatePostImages(postId, newImages, remainImageIds, user.getId());
        return ResponseEntity.ok(SuccessResponse.of("이미지 수정 성공"));
    }

    @DeleteMapping
    @Operation(
        summary = "게시글 모든 이미지 삭제",
        description = "특정 게시글의 모든 이미지를 삭제합니다. S3에서도 파일이 삭제됩니다. (게시글 작성자만 가능)"
    )
    @SecurityRequirement(name = "Authorization")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "이미지 삭제 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 필요"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 부족 (게시글 작성자가 아님)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "게시글을 찾을 수 없음"
        )
    })
    public ResponseEntity<SuccessResponse> deleteAllImages(
        @AuthenticationPrincipal CustomUserDetails user,
        @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId
    ) {
        postImageService.deleteAllPostImages(postId, user.getId());
        return ResponseEntity.ok(SuccessResponse.of("모든 이미지 삭제 성공"));
    }

    @DeleteMapping("/{imageId}")
    @Operation(
        summary = "개별 이미지 삭제",
        description = "특정 게시글의 개별 이미지를 삭제합니다. S3에서도 파일이 삭제됩니다. (게시글 작성자만 가능)"
    )
    @SecurityRequirement(name = "Authorization")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "이미지 삭제 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 필요"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 부족 (게시글 작성자가 아님)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "게시글 또는 이미지를 찾을 수 없음"
        )
    })
    public ResponseEntity<SuccessResponse> deleteImage(
        @AuthenticationPrincipal CustomUserDetails user,
        @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId,
        @Parameter(description = "이미지 ID", example = "1") @PathVariable Long imageId
    ) {
        postImageService.deletePostImage(postId, imageId, user.getId());
        return ResponseEntity.ok(SuccessResponse.of("이미지 삭제 성공"));
    }
    
} 