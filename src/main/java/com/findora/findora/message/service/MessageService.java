package com.findora.findora.message.service;

import com.findora.findora.message.dto.MessageRequestDto;
import com.findora.findora.message.dto.MessageResponseDto;
import com.findora.findora.message.model.Message;
import com.findora.findora.message.repository.MessageRepository;
import com.findora.findora.messageauth.model.MessageAuth;
import com.findora.findora.messageauth.service.MessageAuthService;
import com.findora.findora.messageauth.repository.MessageAuthRepository;
import com.findora.findora.users.model.User;
import com.findora.findora.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageAuthRepository messageAuthRepository;
    private final MessageAuthService messageAuthService;
    @Transactional
    public MessageResponseDto sendMessage(Long senderId, MessageRequestDto dto) {
        MessageAuth messageAuth = messageAuthRepository.findByUserId(senderId)
                .orElseThrow(() -> new IllegalArgumentException("쪽지 권한이 없습니다."));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("보내는 회원이 존재하지 않습니다."));
        User receiver = userRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("받는 회원이 존재하지 않습니다."));

        Message message = new Message(
                sender,
                receiver,
                dto.getContent(),
                LocalDateTime.now(),
                false
        );
        messageRepository.save(message);
        messageAuthService.useAuthority(senderId);
        return new MessageResponseDto(message);
    }

    @Transactional
    public Optional<MessageResponseDto> getMessage(Long messageId, Long userId) {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isEmpty()) return Optional.empty();
        Message message = optionalMessage.get();
        // 본인이 받은 쪽지만 조회 가능
        if (!message.getReceiver().getId().equals(userId)) {
            return Optional.empty();
        }
        // 읽음 처리
        if (message.getIsRead() == null || !message.getIsRead()) {
            message.setIsRead(true);
        }
        return Optional.of(new MessageResponseDto(message));
    }

    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("쪽지를 찾을 수 없습니다."));
        if (!message.getReceiver().getId().equals(userId)) {
            throw new SecurityException("본인 쪽지만 읽음 처리할 수 있습니다.");
        }
        message.setIsRead(true);
    }

    @Transactional(readOnly = true)
    public List<MessageResponseDto> getReceivedMessages(Long userId) {
        List<Message> messages = messageRepository.findAllByReceiverIdOrderBySentAtDesc(userId);
        return messages.stream().map(MessageResponseDto::new).toList();
    }

    @Transactional(readOnly = true)
    public List<MessageResponseDto> getSentMessages(Long userId) {
        List<Message> messages = messageRepository.findAllBySenderIdOrderBySentAtDesc(userId);
        System.out.println("messages: " + messages);
        System.out.println("messages.size(): " + messages.size());
        System.out.println("userId: " + userId);
        return messages.stream().map(MessageResponseDto::new).toList();
    }

    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("쪽지를 찾을 수 없습니다."));
        if (!message.getSender().getId().equals(userId) && !message.getReceiver().getId().equals(userId)) {
            throw new SecurityException("본인 쪽지만 삭제할 수 있습니다.");
        }
        messageRepository.delete(message);
    }

    // 필요시 쪽지 목록, 읽음 처리 등 추가
} 