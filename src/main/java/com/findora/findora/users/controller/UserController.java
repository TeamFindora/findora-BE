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

import com.findora.findora.agreement.dto.AgreementRequestDTO;
import com.findora.findora.emailverification.service.EmailVerificationService;
import com.findora.findora.users.dto.UserRegisterRequestDTO;
import com.findora.findora.users.model.User;
import com.findora.findora.users.repository.UserRepository;
import com.findora.findora.users.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "사용자 관리 API")
public class UserController {
    
    private final UserService userService;
    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    
    @Operation(summary = "사용자 등록", description = "새로운 사용자를 등록합니다. 필수 약관 동의가 필요합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 등록 성공",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"message\": \"사용자가 성공적으로 등록되었습니다.\", \"userId\": 1, \"loginId\": \"user123\", \"email\": \"user@example.com\", \"nickname\": \"사용자\"}"))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 필수 약관 미동의",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"필수 약관에 동의해야 합니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"서버 오류가 발생했습니다.\"}")))
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Parameter(description = "사용자 등록 정보", required = true)
            @RequestBody UserRegisterRequestDTO request) {
        try {
             boolean allRequiredAgreed = request.getAgreements().stream()
            .filter(a -> a.getType().equals("SERVICE") || a.getType().equals("PRIVACY"))
            .allMatch(AgreementRequestDTO::isAgreed);

            if (!allRequiredAgreed) {
                return ResponseEntity.badRequest().body(Map.of("error", "필수 약관에 동의해야 합니다."));
            }
            User user = userService.registerUser(request);
            
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

    @Operation(summary = "사용자 로그인", description = "loginId와 password로 로그인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"userId\": 1, \"loginId\": \"user123\", \"nickname\": \"사용자\", \"role\": \"USER\"}"))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"loginId와 password만 입력해야 합니다.\"}")))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Parameter(description = "로그인 정보", required = true,
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"loginId\": \"user123\", \"password\": \"password123\"}")))
            @RequestBody Map<String, String> request) {
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
    
    @Operation(summary = "모든 사용자 조회", description = "등록된 모든 사용자 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "사용자 목록 조회 성공")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @Operation(summary = "ID로 사용자 조회", description = "사용자 ID로 특정 사용자를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 조회 성공"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "이메일로 사용자 조회", description = "이메일 주소로 특정 사용자를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 조회 성공"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(
            @Parameter(description = "이메일 주소", required = true, example = "user@example.com")
            @PathVariable String email) {
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "닉네임 중복 확인", description = "닉네임의 중복 여부를 확인합니다.")
    @ApiResponse(responseCode = "200", description = "중복 확인 완료",
        content = @Content(mediaType = "application/json",
            examples = @ExampleObject(value = "{\"exists\": false}")))
    @GetMapping("/check-nickname")
    public ResponseEntity<?> checkNickname(
            @Parameter(description = "확인할 닉네임", required = true, example = "사용자")
            @RequestParam String nickname) {
        boolean exists = userRepository.existsByNickname(nickname);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @Operation(summary = "아이디 중복 확인", description = "로그인 ID의 중복 여부를 확인합니다.")
    @ApiResponse(responseCode = "200", description = "중복 확인 완료",
        content = @Content(mediaType = "application/json",
            examples = @ExampleObject(value = "{\"exists\": false}")))
    @GetMapping("/check-loginid")
    public ResponseEntity<?> checkLoginId(
            @Parameter(description = "확인할 로그인 ID", required = true, example = "user123")
            @RequestParam String loginId) {
        boolean exists = userRepository.existsByLoginId(loginId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @Operation(summary = "이메일 인증코드 발송", description = "이메일로 인증코드를 발송합니다.")
    @ApiResponse(responseCode = "200", description = "인증코드 발송 성공",
        content = @Content(mediaType = "application/json",
            examples = @ExampleObject(value = "{\"message\": \"이메일로 인증코드를 보냈습니다.\"}")))
    @PostMapping("/send-email-code")
    public ResponseEntity<?> sendEmailCode(
            @Parameter(description = "이메일 주소", required = true,
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"email\": \"user@example.com\"}")))
            @RequestBody Map<String, String> request) {
        String email = request.get("email");
        emailVerificationService.sendCode(email);
        return ResponseEntity.ok(Map.of("message", "이메일로 인증코드를 보냈습니다."));
    }
    
    @Operation(summary = "이메일 인증", description = "사용자의 이메일을 인증합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이메일 인증 성공",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"message\": \"이메일이 성공적으로 인증되었습니다.\"}"))),
        @ApiResponse(responseCode = "400", description = "인증 실패",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"인증에 실패했습니다.\"}")))
    })
    @PostMapping("/{id}/verify-email")
    public ResponseEntity<?> verifyEmail(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            userService.verifyEmail(id);
            return ResponseEntity.ok(Map.of("message", "이메일이 성공적으로 인증되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @Operation(summary = "비밀번호 변경", description = "사용자의 비밀번호를 변경합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"message\": \"비밀번호가 성공적으로 변경되었습니다.\"}"))),
        @ApiResponse(responseCode = "400", description = "변경 실패",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"비밀번호 변경에 실패했습니다.\"}")))
    })
    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "새 비밀번호", required = true,
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"newPassword\": \"newPassword123\"}")))
            @RequestBody Map<String, String> request) {
        try {
            String newPassword = request.get("newPassword");
            userService.changePassword(id, newPassword);
            return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @Operation(summary = "닉네임 변경", description = "사용자의 닉네임을 변경합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "닉네임 변경 성공",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"message\": \"닉네임이 성공적으로 변경되었습니다.\"}"))),
        @ApiResponse(responseCode = "400", description = "변경 실패",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"닉네임 변경에 실패했습니다.\"}")))
    })
    @PutMapping("/{id}/nickname")
    public ResponseEntity<?> changeNickname(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "새 닉네임", required = true,
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"newNickname\": \"새닉네임\"}")))
            @RequestBody Map<String, String> request) {
        try {
            String newNickname = request.get("newNickname");
            userService.changeNickname(id, newNickname);
            return ResponseEntity.ok(Map.of("message", "닉네임이 성공적으로 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    
} 