package com.findora.findora.postsimage.service;

import com.findora.findora.common.service.S3Service;
import com.findora.findora.posts.model.Post;
import com.findora.findora.posts.repository.PostRepository;
import com.findora.findora.postsimage.dto.PostImageResponseDto;
import com.findora.findora.postsimage.model.PostImage;
import com.findora.findora.postsimage.repository.PostImageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@Tag(name = "PostImage Service", description = "게시글 이미지 관리 서비스")
public class PostImageService {
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final S3Service s3Service;

    @Operation(
        summary = "게시글 이미지 업로드",
        description = "특정 게시글에 이미지를 업로드합니다. 최대 10개까지 업로드 가능합니다."
    )
    public List<String> savePostImages(
        @Parameter(description = "게시글 ID", example = "1") Long postId,
        @Parameter(description = "업로드할 이미지 파일들") List<MultipartFile> images,
        @Parameter(description = "사용자 ID") Long userId
    ) {
        // 1. 게시글 존재 여부 확인 및 권한 검증
        Post post = validatePostOwnership(postId, userId);

        // 2. 이미지가 없는 경우 빈 리스트 반환
        if (images == null || images.isEmpty()) {
            log.info("업로드할 이미지가 없습니다: postId={}", postId);
            return new ArrayList<>();
        }

        log.debug("이미지 저장 프로세스 시작: postId={}, 이미지 개수={}", postId, images.size());

        //이미지 유효성 검사
        validateImageUpload(postId, images);

        try {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile image : images) {
                log.debug("이미지 파일 저장 시작: filename={}", image.getOriginalFilename());
                
                // S3에 파일 업로드
                String folderPath = String.format("posts/%d", postId);
                String imageUrl = s3Service.uploadFile(image, folderPath);
                log.info("이미지 업로드 성공 - URL: {}", imageUrl);

                PostImage postImage = PostImage.builder()
                        .post(post)
                        .imageUrl(imageUrl)
                        .build();

                postImageRepository.save(postImage);
                log.info("이미지 정보 DB 저장 성공 - imageId: {}", postImage.getId());

                imageUrls.add(imageUrl);
            }

            return imageUrls;
        } catch (Exception e) {
            log.error("이미지 저장 중 오류 발생", e);
            throw new RuntimeException("이미지 저장 중 오류가 발생했습니다", e);
        }
    }

    @Operation(
        summary = "게시글 이미지 목록 조회",
        description = "특정 게시글의 모든 이미지를 조회합니다."
    )
    @Transactional(readOnly = true)
    public List<PostImageResponseDto> getPostImages(
        @Parameter(description = "게시글 ID", example = "1") Long postId
    ) {
        // 게시글 존재 여부만 확인 (조회는 모든 사용자가 가능)
        postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
                
        return postImageRepository.findByPostId(postId).stream()
                .map(PostImageResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Operation(
        summary = "게시글 이미지 수정",
        description = "게시글의 이미지를 수정합니다. 기존 이미지를 삭제하고 새로운 이미지를 추가할 수 있습니다."
    )
    public List<String> updatePostImages(
        @Parameter(description = "게시글 ID", example = "1") Long postId,
        @Parameter(description = "새로 추가할 이미지 파일들") List<MultipartFile> newImagesList,
        @Parameter(description = "유지할 기존 이미지 ID 목록") List<Long> remainImageIds,
        @Parameter(description = "사용자 ID") Long userId
    ) {
        // 1. 게시글 존재 여부 확인 및 권한 검증
        validatePostOwnership(postId, userId);
        List<String> updatedImageUrls = new ArrayList<>();

        // 이미지id로 삭제
        deleteImagesExcept(postId, remainImageIds);

        // 새로운 이미지 추가
        validateImageUpload(postId, newImagesList);
        if (newImagesList != null && !newImagesList.isEmpty()) {
            for (MultipartFile image : newImagesList) {
                if (!image.isEmpty()) {
                    String folderPath = String.format("posts/%d", postId);
                    String imageUrl = s3Service.uploadFile(image, folderPath);
                    PostImage postImage = PostImage.builder()
                            .imageUrl(imageUrl)
                            .post(postRepository.getReferenceById(postId))
                            .build();
                    postImageRepository.save(postImage);
                    updatedImageUrls.add(imageUrl);
                }
            }
        }
        return updatedImageUrls;
    }


    @Operation(
        summary = "게시글 모든 이미지 삭제",
        description = "특정 게시글의 모든 이미지를 삭제합니다. S3에서도 파일이 삭제됩니다."
    )
    public void deleteAllPostImages(
        @Parameter(description = "게시글 ID", example = "1") Long postId,
        @Parameter(description = "사용자 ID") Long userId
    ) {
        // 1. 게시글 존재 여부 확인 및 권한 검증
        validatePostOwnership(postId, userId);

        List<PostImage> images = postImageRepository.findByPostId(postId);

        // 2. 각 이미지 파일 삭제
        for (PostImage image : images) {
            try {
                s3Service.deleteFile(image.getImageUrl());
            } catch (Exception e) {
                log.error("이미지 파일 삭제 실패: {}", image.getImageUrl(), e);
            }
        }

        // 3. DB에서 이미지 정보 삭제
        postImageRepository.deleteAll(images);
        log.info("게시글의 모든 이미지 삭제 완료: postId={}", postId);
    }

    @Operation(
        summary = "개별 이미지 삭제",
        description = "특정 게시글의 개별 이미지를 삭제합니다. S3에서도 파일이 삭제됩니다."
    )
    public void deletePostImage(
        @Parameter(description = "게시글 ID", example = "1") Long postId,
        @Parameter(description = "이미지 ID", example = "1") Long imageId,
        @Parameter(description = "사용자 ID") Long userId
    ) {
        // 1. 게시글 존재 여부 확인 및 권한 검증
        validatePostOwnership(postId, userId);

        // 2. 이미지 존재 여부 확인 및 게시글 소유권 검증
        PostImage postImage = postImageRepository.findByIdAndPostId(imageId, postId)
                .orElseThrow(() -> {
                    log.error("이미지를 찾을 수 없습니다: imageId={}, postId={}", imageId, postId);
                    return new IllegalArgumentException("이미지를 찾을 수 없습니다.");
                });

        // 3. S3에서 이미지 파일 삭제
        try {
            s3Service.deleteFile(postImage.getImageUrl());
            log.info("S3에서 이미지 파일 삭제 완료: {}", postImage.getImageUrl());
        } catch (Exception e) {
            log.error("S3에서 이미지 파일 삭제 실패: {}", postImage.getImageUrl(), e);
            throw new RuntimeException("이미지 파일 삭제 중 오류가 발생했습니다.", e);
        }

        // 4. DB에서 이미지 정보 삭제
        postImageRepository.delete(postImage);
        log.info("개별 이미지 삭제 완료: imageId={}, postId={}", imageId, postId);
    }
    private void deleteImagesExcept(Long postId, List<Long> remainImageIds) {
        postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        List<PostImage> existingImages = postImageRepository.findByPostId(postId);

        if (remainImageIds != null && !remainImageIds.isEmpty()) {
            Set<Long> existingImageIds = existingImages.stream()
                    .map(PostImage::getId)
                    .collect(Collectors.toSet());

            List<Long> nonExistentImageIds = remainImageIds.stream()
                    .filter(id -> !existingImageIds.contains(id))
                    .collect(Collectors.toList());


            if (!nonExistentImageIds.isEmpty()) {
                String invalidIdsStr = nonExistentImageIds
                        .stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "));
                throw new IllegalArgumentException(
                        String.format("이미지 ID %s는 게시글 %d에 존재하지 않습니다.", invalidIdsStr, postId)
                );
            }
        }

        // 4. remainImageIds에 포함되지 않은 이미지만 삭제
        for (PostImage image : existingImages) {
            if (remainImageIds == null || !remainImageIds.contains(image.getId())) {
                try {
                    s3Service.deleteFile(image.getImageUrl());
                    postImageRepository.delete(image);
                    log.info("이미지 삭제 완료: imageId={}, postId={}", image.getId(), postId);
                } catch (Exception e) {
                    log.error("이미지 파일 삭제 실패: {}", image.getImageUrl(), e);
                }
            }
        }
    }


    // 허용되는 이미지 타입 검사
    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/webp");
    }


    //이미지 업로드 유효성 검사
    private void validateImageUpload(Long postId, List<MultipartFile> images) {

        if (images == null) {
            return; // 또는 images = new ArrayList<>();
        }
        // 기존 이미지 수 확인
        int existingImagesCount = postImageRepository.countByPostId(postId);
        // 새로 추가할 이미지와 기존 이미지의 총 개수가 제한을 초과하는지 확인
        if (existingImagesCount + images.size() > 10) {
            throw new IllegalArgumentException(
                    String.format("이미지는 게시글당 최대 10개까지만 업로드 가능합니다. " +
                                    "현재 %d개가 있어 %d개만 추가로 업로드할 수 있습니다.",
                            existingImagesCount,
                            (10 - existingImagesCount))
            );
        }

        // 이미지 파일 형식 검사
        for (MultipartFile file : images) {
            String contentType = file.getContentType();
            if (contentType == null || !isValidImageType(contentType)) {
                throw new IllegalArgumentException(
                        String.format("'%s' 파일은 허용되지 않는 형식입니다. " +
                                        "허용되는 형식: JPG, JPEG, PNG, GIF, WEBP",
                                file.getOriginalFilename())
                );
            }

        }
    }

    /**
     * 게시글 소유권 검증
     * @param postId 게시글 ID
     * @param userId 사용자 ID
     * @return 검증된 Post 엔티티
     * @throws IllegalArgumentException 게시글이 없거나 소유자가 아닌 경우
     */
    private Post validatePostOwnership(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("게시글을 찾을 수 없습니다: postId={}", postId);
                    return new IllegalArgumentException("게시글을 찾을 수 없습니다.");
                });

        // 게시글 작성자와 요청한 사용자가 다른 경우
        if (!post.getUser().getId().equals(userId)) {
            log.error("게시글 소유자가 아닙니다: postId={}, userId={}, postOwnerId={}", 
                     postId, userId, post.getUser().getId());
            throw new IllegalArgumentException("게시글 작성자만 이미지를 관리할 수 있습니다.");
        }

        return post;
    }
}
