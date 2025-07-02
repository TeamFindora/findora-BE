package com.findora.findora.emailverification.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.findora.findora.emailverification.service.EmailVerificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor    
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

     // 인증코드 발송
    @PostMapping("/send-code")
    @Operation(summary = "이메일 인증코드 발송", description = "입력한 이메일로 인증코드를 전송합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "이메일 입력",
        required = true,
        content = @Content(
            examples = @ExampleObject(
                name = "요청 예시",
                value = "{ \"email\": \"user@example.com\" }"
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이메일 발송 성공",
            content = @Content(examples = @ExampleObject(value = """
                {
                  \"message\": \"이메일로 인증코드를 보냈습니다.\"
                }
            """)))
    })
    public ResponseEntity<?> sendCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        emailVerificationService.sendCode(email);
        return ResponseEntity.ok(Map.of("message", "이메일로 인증코드를 보냈습니다."));
    }

    // 인증코드 검증
    @PostMapping("/verify-code")
    @Operation(summary = "이메일 인증코드 검증", description = "입력한 이메일과 인증코드가 일치하는지 검증합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "이메일과 인증코드 입력",
        required = true,
        content = @Content(
            examples = @ExampleObject(
                name = "요청 예시",
                value = "{ \"email\": \"user@example.com\", \"code\": \"123456\" }"
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "인증 성공 여부 반환",
            content = @Content(examples = @ExampleObject(value = """
                {
                  \"verified\": true
                }
            """)))
    })
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        boolean result = emailVerificationService.verifyCode(email, code);
        return ResponseEntity.ok(Map.of("verified", result));
    }
}