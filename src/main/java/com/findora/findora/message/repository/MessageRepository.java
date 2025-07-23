package com.findora.findora.message.repository;

import com.findora.findora.message.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // 필요시 쿼리 메서드 추가
    org.springframework.data.domain.Page<Message> findAllByReceiverIdOrderBySentAtDesc(Long receiverId, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Message> findAllBySenderIdOrderBySentAtDesc(Long senderId, org.springframework.data.domain.Pageable pageable);
    java.util.List<Message> findAllByReceiverIdOrderBySentAtDesc(Long receiverId);
    java.util.List<Message> findAllBySenderIdOrderBySentAtDesc(Long senderId);
} 