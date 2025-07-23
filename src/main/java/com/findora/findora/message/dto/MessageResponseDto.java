package com.findora.findora.message.dto;

import com.findora.findora.message.model.Message;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MessageResponseDto {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private LocalDateTime sentAt;
    private Boolean isRead;

    public MessageResponseDto(Message message) {
        this.id = message.getId();
        this.senderId = message.getSender().getId();
        this.receiverId = message.getReceiver().getId();
        this.content = message.getContent();
        this.sentAt = message.getSentAt();
        this.isRead = message.getIsRead();
    }
} 