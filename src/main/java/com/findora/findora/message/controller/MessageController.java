package com.findora.findora.message.controller;

import com.findora.findora.message.dto.MessageRequestDto;
import com.findora.findora.message.dto.MessageResponseDto;
import com.findora.findora.message.service.MessageService;
import com.findora.findora.users.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import com.findora.findora.common.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import java.util.List;

@Tag(name = "쪽지", description = "쪽지(Message) API")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @Operation(summary = "쪽지 보내기")
    @PostMapping
    public ResponseEntity<MessageResponseDto> sendMessage(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody MessageRequestDto dto) {
        MessageResponseDto response = messageService.sendMessage(user.getId(), dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "쪽지 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<?> getMessage(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long id) {
        return messageService.getMessage(id, user.getId())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(SuccessResponse.of("존재하지 않는 쪽지입니다.")));
    }

    @Operation(summary = "쪽지 읽음 처리")
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long id) {
        messageService.markAsRead(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "받은 쪽지 목록 조회")
    @GetMapping("/received")
    public ResponseEntity<?> getReceivedMessages(
            @AuthenticationPrincipal CustomUserDetails user) {
        List<MessageResponseDto> messages = messageService.getReceivedMessages(user.getId());
        if (messages.isEmpty()) {
            return ResponseEntity.ok(SuccessResponse.of("받은 쪽지가 없습니다."));
        }
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "보낸 쪽지 목록 조회")
    @GetMapping("/sent")
    public ResponseEntity<?> getSentMessages(
            @AuthenticationPrincipal CustomUserDetails user) {
        List<MessageResponseDto> messages = messageService.getSentMessages(user.getId());
        boolean allNull = messages.stream().allMatch(java.util.Objects::isNull);
        if (messages.isEmpty() || allNull) {
            return ResponseEntity.ok(SuccessResponse.of("보낸 쪽지가 없습니다."));
        }
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "쪽지 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long id) {
        messageService.deleteMessage(id, user.getId());
        return ResponseEntity.noContent().build();
    }
} 