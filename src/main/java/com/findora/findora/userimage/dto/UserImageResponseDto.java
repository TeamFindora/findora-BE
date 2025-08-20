package com.findora.findora.userimage.dto;

import com.findora.findora.userimage.model.UserImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class UserImageResponseDto {
    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "이미지 URL", example = "https://findora-images.s3.ap-northeast-2.amazonaws.com/users/{userid}/uuid-image.jpg")
    private String imageUrl;

    @Schema(description = "이미지 승인 상태", example = "PENDING", allowableValues = {"PENDING", "APPROVED", "REJECTED"})
    private String status;

    @Schema(description = "거절 사유", example = "이미지 품질이 낮습니다.")
    private String message;  // 승인 시 null, 거절 시 사유

    public static UserImageResponseDto fromEntity(UserImage userImage) {
        return new UserImageResponseDto(
                userImage.getUser().getId(),
                userImage.getImageUrl(),
                userImage.getStatus().name(),
                userImage.getStatus() == UserImage.VerificationStatus.REJECTED
                        ? userImage.getRejectReason()
                        : null
        );
    }


}
