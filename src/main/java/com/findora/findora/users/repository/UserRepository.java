package com.findora.findora.users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.findora.findora.users.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);
    
    // 닉네임으로 사용자 찾기
    Optional<User> findByNickname(String nickname);
    
    // 이메일 존재 여부 확인
    boolean existsByEmail(String email);

    // 로그인 아이디로 사용자 찾기
    Optional<User> findByLoginId(String loginId);
    
    // 닉네임 존재 여부 확인
    boolean existsByNickname(String nickname);
    

    // 로그인 아이디 존재 여부 확인
    boolean existsByLoginId(String loginId);
} 