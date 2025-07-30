package com.findora.findora.messageauth.controller;


import com.findora.findora.messageauth.dto.MessageAuthResponseDto;
import com.findora.findora.messageauth.model.MessageAuth;
import com.findora.findora.messageauth.service.MessageAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/message-auth")
@Tag(name = "쪽지 권한", description = "쪽지 권한 API")
public class MessageAuthController {

    private final MessageAuthService messageAuthService;


    @Operation(summary = "사용자 등록", description = "결제 시 쪽지 권한 5회 추가 (기존 유저는 누적, 신규 유저는 생성)")
    @PostMapping("/grant/{userId}")
    public ResponseEntity<MessageAuthResponseDto> grantAuthority(@PathVariable Long userId) {
        return ResponseEntity.ok(messageAuthService.grantAuth(userId));
    }

    @Operation(summary = "무제한 쪽지 사용자 등록")
    @PostMapping("/grant/unlimited/{userId}")
    public ResponseEntity<String> forceGrantUnlimited(@RequestParam Long userId) {
        messageAuthService.grantUnlimitedAuth(userId);
        return ResponseEntity.ok("무제한 쪽지 권한 사용자 등록");
    }

    @Operation(summary = "쪽지 사용", description = "쪽지 보낼 때 count 1회 차감")
    @PostMapping("/use/{userId}")
    public ResponseEntity<String> useAuthority(@PathVariable Long userId) {
        messageAuthService.useAuthority(userId);
        return ResponseEntity.ok("쪽지 1회 사용 완료");
    }

    @Operation(summary = "남은 쪽지 개수 조회", description = "사용자의 남은 쪽지 개수 조회")
    @GetMapping("count/{userId}")
    public ResponseEntity<MessageAuthResponseDto> getAuthority(@PathVariable Long userId) {
        return ResponseEntity.ok(messageAuthService.getAuthority(userId));

    }

    @Operation(summary = "전체 사용자 조회", description = "모든 사용자 조회")
    @GetMapping("/all")
    public ResponseEntity<List<MessageAuthResponseDto>> getAllAuthorities() {
        List<MessageAuth> authorityList = messageAuthService.getAllAuthorizedUsers();

        List<MessageAuthResponseDto> result = authorityList.stream()
                .map(MessageAuthResponseDto::fromEntity)
                .toList();

        return ResponseEntity.ok(result);
    }
}
