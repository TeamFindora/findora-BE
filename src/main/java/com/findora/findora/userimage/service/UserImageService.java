package com.findora.findora.userimage.service;

import com.findora.findora.common.service.S3Service;
import com.findora.findora.messageauth.service.MessageAuthService;
import com.findora.findora.postsimage.service.FileService;
import com.findora.findora.userimage.dto.UserImageResponseDto;
import com.findora.findora.userimage.model.UserImage;
import com.findora.findora.userimage.repository.UserImageRepository;
import com.findora.findora.users.model.User;
import com.findora.findora.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserImageService {
    private final UserImageRepository userImageRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final MessageAuthService messageAuthService; //승인 후 무제한 쪽지부여

    // user 학생증 업로드
    @Transactional
    public UserImageResponseDto uploadUserImage(Long userId, MultipartFile image) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 기존 UserImage 확인
        Optional<UserImage> existingImage = userImageRepository.findByUserId(userId);

        if (existingImage.isPresent()) {
            UserImage existing = existingImage.get();
            log.info("기존 UserImage 발견 거절, 대기중인 상태는 삭제 후 재업로드 예정 - userId: {}, status: {}",
                    userId, existing.getStatus());


            // 이미 승인된 사용자인 경우
            if (existing.getStatus() == UserImage.VerificationStatus.APPROVED) {
                throw new IllegalArgumentException("이미 승인된 사용자 입니다.");
            }

            // 거절되거나 대기 중인 이미지가 있으면 삭제
            if (existing.getStatus() == UserImage.VerificationStatus.REJECTED ||
                    existing.getStatus() == UserImage.VerificationStatus.PENDING) {

                // S3에서 기존 이미지 파일 삭제
                try {
                    s3Service.deleteFile(existing.getImageUrl());
                    log.info("기존 이미지 S3에서 삭제 완료 - URL: {}", existing.getImageUrl());
                } catch (Exception e) {
                    log.warn("S3 파일 삭제 실패 - URL: {}, 오류: {}", existing.getImageUrl(), e.getMessage());
                }

                // DB에서 기존 이미지 삭제
                userImageRepository.delete(existing);
                log.info("기존 UserImage 삭제 완료 - userId: {}, status: {}", userId, existing.getStatus());
            }
        }
        validateImageUpload(image); // 이미지 업로드 유효성 검사
        String folderPath = String.format("STUDENT/%d", userId);
        String imageUrl = s3Service.uploadFile(image, folderPath);
        log.info("이미지 업로드 성공 - URL: {}", imageUrl);

        UserImage userImage = UserImage.builder()
                .user(user)
                .imageUrl(imageUrl)
                .build();

        user.changeRole(User.Role.WAITING);
        userImageRepository.save(userImage);

        return UserImageResponseDto.fromEntity(userImage);
    }

    // 학생증 이미지 승인
    @Transactional
    public UserImageResponseDto approve(Long userId) {
        UserImage image = userImageRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 기존 UserImage 확인
        if (image.getStatus() == UserImage.VerificationStatus.REJECTED) {
            throw new IllegalArgumentException("거절된 학생입니다. 이미지를 다시 업로드해주세요.");
        } else if (image.getStatus() == UserImage.VerificationStatus.APPROVED) {
            throw new IllegalArgumentException("이미 승인 완료된 학생입니다.");
        }

        image.approve();
        image.getUser().changeRole(User.Role.STUDENT);
        log.info("학생증 승인 - userId: {}", userId);

        // 승인 처리 후 무제한 권한 부여
        messageAuthService.grantUnlimitedAuth(userId);
        return UserImageResponseDto.fromEntity(image);
    }

    // 학생증 이미지 거절
    @Transactional
    public UserImageResponseDto reject(Long userId, String reason) {
        UserImage image = userImageRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        image.reject(reason);
        image.getUser().changeRole(User.Role.USER);
        log.info("학생증 이미지 거절 - userId: {}, 거절사유: {}", userId, reason);

        return UserImageResponseDto.fromEntity(image);
    }

    // 승인된 학생 목록 조회
    public List<UserImageResponseDto> getApprovedStudent() {
        return userImageRepository.findByStatus(UserImage.VerificationStatus.APPROVED).stream()
                .filter(img -> img.getUser().getRole() == User.Role.STUDENT)
                .map(UserImageResponseDto::fromEntity)
                .toList();
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
    private void validateImageUpload(MultipartFile image) {
        // 이미지 파일 형식 검사
        String contentType = image.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            throw new IllegalArgumentException(
                    String.format("'%s' 파일은 허용되지 않는 형식입니다. " +
                                    "허용되는 형식: JPG, JPEG, PNG, GIF, WEBP",
                            image.getOriginalFilename())
            );
        }
    }
}

