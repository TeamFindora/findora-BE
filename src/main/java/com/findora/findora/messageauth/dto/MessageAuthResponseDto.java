package com.findora.findora.messageauth.dto;

import com.findora.findora.messageauth.model.MessageAuth;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageAuthResponseDto {

    private Long userId;
    private int count;

    public static MessageAuthResponseDto fromEntity(MessageAuth messageAuth) {
        return MessageAuthResponseDto.builder()
                .userId(messageAuth.getUser().getId())
                .count(messageAuth.getCount())
                .build();
    }
}
