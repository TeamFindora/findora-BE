package com.findora.findora.agreement.dto;

import lombok.Getter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
public class AgreementRequestDTO {
    @Schema(example = "SERVICE")
    private String type;
    @Schema(example = "true")
    private boolean agreed;
}