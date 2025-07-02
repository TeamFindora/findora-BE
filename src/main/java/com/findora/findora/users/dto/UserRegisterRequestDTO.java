package com.findora.findora.users.dto;

import java.util.List;

import com.findora.findora.agreement.dto.AgreementRequestDTO;

import lombok.Getter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
public class UserRegisterRequestDTO {
    @Schema(example = "user123")
    private String loginId;
    @Schema(example = "password123")
    private String password;
    @Schema(example = "홍길동")
    private String nickname;
    @Schema(example = "user@example.com")
    private String email;
    @Schema(example = "USER")
    private String role;
    @Schema(description = "약관 동의 목록", implementation = AgreementRequestDTO.class)
    private List<AgreementRequestDTO> agreements; // 이미 정의된 AgreementDto 사용
}
