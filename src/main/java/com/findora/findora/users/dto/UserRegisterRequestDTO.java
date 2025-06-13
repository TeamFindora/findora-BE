package com.findora.findora.users.dto;

import java.util.List;

import com.findora.findora.agreement.dto.AgreementRequestDTO;

import lombok.Getter;
@Getter
public class UserRegisterRequestDTO {
    private String loginId;
    private String password;
    private String nickname;
    private String email;
    private String role;
    private List<AgreementRequestDTO> agreements; // 이미 정의된 AgreementDto 사용
}
