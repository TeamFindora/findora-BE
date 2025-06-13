package com.findora.findora.emailverification.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.findora.findora.emailverification.service.EmailVerificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor    
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

     // 인증코드 발송
    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        emailVerificationService.sendCode(email);
        return ResponseEntity.ok(Map.of("message", "이메일로 인증코드를 보냈습니다."));
    }

    // 인증코드 검증
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        boolean result = emailVerificationService.verifyCode(email, code);
        return ResponseEntity.ok(Map.of("verified", result));
    }
}