package com.findora.findora.agreement.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgreementRequestDTO {
    @Schema(description = "약관 타입", example = "SERVICE", allowableValues = {"SERVICE", "PRIVACY", "MARKETING"})
    private String type;
    
    @Schema(description = "동의 여부", example = "true")
    private boolean agreed;
}