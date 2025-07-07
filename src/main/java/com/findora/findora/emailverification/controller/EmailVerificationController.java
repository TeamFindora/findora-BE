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
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    /**
     * 인증코드 발송
     */
    @PostMapping("/send-code")
    @Operation(summary = "이메일 인증코드 발송", description = "입력한 이메일로 인증코드를 전송합니다. 기존 코드가 있으면 무효화되고 새로운 코드가 발송됩니다.")
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
                  "success": true,
                  "message": "인증코드가 이메일로 전송되었습니다. 10분 후 만료됩니다.",
                  "expirationMinutes": 10
                }
            """))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(examples = @ExampleObject(value = """
                {
                  "success": false,
                  "message": "이미 인증된 이메일입니다."
                }
            """))),
        @ApiResponse(responseCode = "500", description = "이메일 발송 실패",
            content = @Content(examples = @ExampleObject(value = """
                {
                  "success": false,
                  "message": "이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요."
                }
            """)))
    })
    public ResponseEntity<?> sendCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "이메일 주소를 입력해주세요."
                ));
            }
            
            emailVerificationService.sendCode(email.trim());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "인증코드가 이메일로 전송되었습니다. 10분 후 만료됩니다.",
                "expirationMinutes", 10
            ));
            
        } catch (IllegalArgumentException e) {
            log.warn("인증코드 발송 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("인증코드 발송 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요."
            ));
        }
    }

    /**
     * 인증코드 검증
     */
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
        @ApiResponse(responseCode = "200", description = "인증 성공",
            content = @Content(examples = @ExampleObject(value = """
                {
                  "success": true,
                  "verified": true,
                  "message": "이메일 인증이 완료되었습니다."
                }
            """))),
        @ApiResponse(responseCode = "400", description = "인증 실패",
            content = @Content(examples = @ExampleObject(value = """
                {
                  "success": false,
                  "verified": false,
                  "message": "인증코드가 일치하지 않습니다."
                }
            """)))
    })
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = request.get("code");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "verified", false,
                    "message", "이메일 주소를 입력해주세요."
                ));
            }
            
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "verified", false,
                    "message", "인증코드를 입력해주세요."
                ));
            }
            
            boolean result = emailVerificationService.verifyCode(email.trim(), code.trim());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "verified", result,
                "message", "이메일 인증이 완료되었습니다."
            ));
            
        } catch (IllegalArgumentException e) {
            log.warn("인증코드 검증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "verified", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("인증코드 검증 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "verified", false,
                "message", "인증 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            ));
        }
    }
}