package com.findora.findora.users.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.findora.findora.emailverification.service.EmailVerificationService;
import com.findora.findora.users.model.User;
import com.findora.findora.users.repository.UserRepository;
import com.findora.findora.users.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    // 사용자 등록
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> request) {
        try {
            String loginId = request.get("loginId");
            String email = request.get("email");
            String password = request.get("password");
            String nickname = request.get("nickname");
            String roleStr = request.get("role");
            
            User.Role role = User.Role.valueOf(roleStr.toUpperCase());
            
            User user = userService.registerUser(loginId, email, password, nickname, role);
            
            return ResponseEntity.ok(Map.of(
                "message", "사용자가 성공적으로 등록되었습니다.",
                "userId", user.getId(),
                "loginId", user.getLoginId(),
                "email", user.getEmail(),
                "nickname", user.getNickname()
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        if (request.size() != 2 || !request.containsKey("loginId") || !request.containsKey("password")) {
        return ResponseEntity.badRequest().body(Map.of("error", "loginId와 password만 입력해야 합니다."));
    }
        String loginId = request.get("loginId");
        String password = request.get("password");
        User user = userService.login(loginId, password);
         return ResponseEntity.ok(Map.of(
            "userId", user.getId(),
            "loginId", user.getLoginId(),
            "nickname", user.getNickname(),
            "role", user.getRole().name()
        ));
    }
    // 모든 사용자 조회
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }
    
    // ID로 사용자 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 이메일로 사용자 조회
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

     // 닉네임 중복확인
    @GetMapping("/check-nickname")
    public ResponseEntity<?> checkNickname(@RequestParam String nickname) {
        boolean exists = userRepository.existsByNickname(nickname);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    // 아이디 중복확인
    @GetMapping("/check-loginid")
    public ResponseEntity<?> checkLoginId(@RequestParam String loginId) {
        boolean exists = userRepository.existsByLoginId(loginId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

     // 이메일 인증코드 발송
    @PostMapping("/send-email-code")
    public ResponseEntity<?> sendEmailCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        emailVerificationService.sendCode(email);
        return ResponseEntity.ok(Map.of("message", "이메일로 인증코드를 보냈습니다."));
    }
    
    // 이메일 인증
    @PostMapping("/{id}/verify-email")
    public ResponseEntity<?> verifyEmail(@PathVariable Long id) {
        try {
            userService.verifyEmail(id);
            return ResponseEntity.ok(Map.of("message", "이메일이 성공적으로 인증되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // 비밀번호 변경
    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String newPassword = request.get("newPassword");
            userService.changePassword(id, newPassword);
            return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // 닉네임 변경
    @PutMapping("/{id}/nickname")
    public ResponseEntity<?> changeNickname(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String newNickname = request.get("newNickname");
            userService.changeNickname(id, newNickname);
            return ResponseEntity.ok(Map.of("message", "닉네임이 성공적으로 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    
} 