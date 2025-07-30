package com.findora.findora.messageauth.service;

import com.findora.findora.messageauth.dto.MessageAuthResponseDto;
import com.findora.findora.messageauth.model.MessageAuth;
import com.findora.findora.messageauth.repository.MessageAuthRepository;
import com.findora.findora.posts.dto.PostResponseDto;
import com.findora.findora.users.model.User;
import com.findora.findora.users.repository.UserRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageAuthService {


    private final MessageAuthRepository messageAuthRepository;
    private final UserRepository userRepository;
    private final Logger log = LoggerFactory.getLogger(MessageAuthService.class);

    // 사용자 등록_쪽지 권한부여 (결제 후 호출)
    // 5회로 설정
    @Transactional
    public MessageAuthResponseDto grantAuth(Long userId) {
        Optional<MessageAuth> optional = messageAuthRepository.findByUserId(userId);

        if (optional.isPresent()) {
            MessageAuth auth = optional.get();
            auth.increaseCount(5);
            log.info("기존 권한 사용자: userId={}, count 증가", userId);
            return MessageAuthResponseDto.fromEntity(auth);
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("사용자를 찾을 수 없습니다: userId={}", userId);
                        return new IllegalArgumentException("사용자를 찾을 수 없습니다.");
                    });
            MessageAuth newAuth = new MessageAuth(user, 5, false); // 5회 권한 부여
            messageAuthRepository.save(newAuth);
            log.info("새 사용자 권한 부여: userId={}, count=5", userId);
            return MessageAuthResponseDto.fromEntity(newAuth);
        }
    }

    // 쪽지 사용 시 count 차감
    @Transactional
    public void useAuthority(Long userId) {
        MessageAuth authority = messageAuthRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("쪽지 권한이 없습니다: userId={}", userId);
                    return new IllegalStateException("쪽지 권한이 없습니다."); //스웨거 런타임 오류 발생해 로그 나타냄
                });


        authority.decreaseCount();

        // 만약 쪽지 횟수가 0이 되면 해당 권한을 삭제(db에서 삭제)
        if (authority.getCount() == 0) {
            messageAuthRepository.delete(authority);
            log.info("권한 삭제됨 (count=0): userId={}", userId);
        }
    }

    // 남은 쪽지 개수 조회
    @Transactional(readOnly = true)
    public MessageAuthResponseDto getAuthority(Long userId) {
        MessageAuth authority = messageAuthRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("쪽지 권한이 없습니다: userId={}", userId);
                    return new IllegalStateException("쪽지 권한이 없습니다."); //스웨거 런타임 오류 발생해 로그 나타냄
                });
        return MessageAuthResponseDto.fromEntity(authority);
    }

    // 전체 사용자 조회
    public List<MessageAuth> getAllAuthorizedUsers() {
        List<MessageAuth> list = messageAuthRepository.findAll();
        if (list.isEmpty()) {
            log.warn("데이터가 존재하지 않습니다. (권한 사용자 없음)");
        } else {
            log.info("전체 권한 사용자 수: {}명", list.size());
        }
        return list;
    }

    //무제한 api 추가
    @Transactional
    public MessageAuthResponseDto grantUnlimitedAuth(Long userId) {
        Optional<MessageAuth> optional = messageAuthRepository.findByUserId(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        MessageAuth auth = MessageAuth.builder()
                .user(user)
                .count(9999)  // 무제한인 경우 사용되지 않음
                .isUnlimited(true)
                .build();

            messageAuthRepository.save(auth);
            return MessageAuthResponseDto.fromEntity(auth);
        }
    }

