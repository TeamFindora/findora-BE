package com.findora.findora.userimage.controller;

import com.findora.findora.common.SuccessResponse;
import com.findora.findora.userimage.dto.UserImageResponseDto;
import com.findora.findora.userimage.service.UserImageService;
import com.findora.findora.users.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(name = "STUDENT 인증", description = "학생증 이미지 업로드 및 승인/거절 API")
@RestController
@RequestMapping("/api/user-images")
@RequiredArgsConstructor
public class UserImageController {
    private final UserImageService userImageService;

    @Operation(summary = "학생증 이미지 업로드", description = "로그인한 사용자가 학생증 이미지를 업로드하면 ROlE이 WAITING으로 변경됩니다.")
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> uploadImage(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "학생증 이미지 파일", required = true)
            @RequestParam("images")MultipartFile image) {

        userImageService.uploadUserImage(user.getId(), image);
        return ResponseEntity.ok(SuccessResponse.of("학생증 업로드 성공하였습니다. 관리자 승인 후 STUDENT로 변경됩니다."));
    }

    @Operation(summary = "학생증 승인", description = "관리자가 학생증 이미지를 검토하여 승인 처리합니다")
    @PutMapping("/{userId}/approve")
    public ResponseEntity<SuccessResponse> approveImage(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long userId) {
        userImageService.approve(userId);
        return ResponseEntity.ok(SuccessResponse.of("관리자의 승인이 완료되었습니다. STUDENT로 변경됩니다."));
    }

    @Operation(summary = "학생증 거절", description = "관리자가 학생증 이미지를 검토하여 거절 처리합니다.")
    @PutMapping("/{userId}/reject")
    public ResponseEntity<SuccessResponse> rejectImage(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long userId,
            @Parameter(description = "거절사유", example = "이미지가 흐릿합니다.")@RequestParam String reason) {
        userImageService.reject(userId, reason);
        return ResponseEntity.ok(SuccessResponse.of("관리자의 승인이 거절되었습니다. USER로 변경됩니다."));
    }

    @Operation(summary = "학생 목록 조회", description = "STUDENT인 모든 학생을 조회합니다.")
    @GetMapping("/students")
    public List<UserImageResponseDto> getAllStudents() {
        return userImageService.getApprovedStudent();
    }
}
