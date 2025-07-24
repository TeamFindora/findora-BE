package com.findora.findora.messageauth.repository;

import com.findora.findora.messageauth.model.MessageAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageAuthRepository extends JpaRepository<MessageAuth, Long> {

    // 사용자 ID로 MessageAuth 엔티티를 조회하는 메소드
    Optional<MessageAuth> findByUserId(Long userId);
    // 전체 사용자 조회
    List<MessageAuth> findAll();
}
