package com.findora.findora.users.dto;

import java.util.List;

import com.findora.findora.agreement.dto.AgreementRequestDTO;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequestDTO {
    @Schema(description = "로그인 ID", example = "user123")
    private String loginId;
    
    @Schema(description = "비밀번호", example = "password123")
    private String password;
    
    @Schema(description = "닉네임", example = "홍길동")
    private String nickname;
    
    @Schema(description = "이메일", example = "user@example.com")
    private String email;
    
    @Schema(description = "사용자 역할", example = "USER")
    private String role;
    
    @ArraySchema(
        schema = @Schema(implementation = AgreementRequestDTO.class),
        arraySchema = @Schema(
            description = "약관 동의 목록 (배열 형태로 전달해야 함)",
            example = """
            [
              {"type": "SERVICE", "agreed": true},
              {"type": "PRIVACY", "agreed": true},
              {"type": "MARKETING", "agreed": false}
            ]
            """
        )
    )
    private List<AgreementRequestDTO> agreements;
}
