package com.findora.findora.users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    // === 삭제된 사용자까지 포함하는 중복 확인 메서드들 ===
    
    // 삭제된 사용자까지 포함하여 닉네임 중복 확인
    @Query(value = "SELECT COUNT(*) FROM users WHERE nickname = ?1", nativeQuery = true)
    Long countByNicknameIncludingDeleted(String nickname);
    
    // 삭제된 사용자까지 포함하여 이메일 중복 확인
    @Query(value = "SELECT COUNT(*) FROM users WHERE email = ?1", nativeQuery = true)
    Long countByEmailIncludingDeleted(String email);
    
    // 삭제된 사용자까지 포함하여 로그인ID 중복 확인
    @Query(value = "SELECT COUNT(*) FROM users WHERE login_id = ?1", nativeQuery = true)
    Long countByLoginIdIncludingDeleted(String loginId);
} 