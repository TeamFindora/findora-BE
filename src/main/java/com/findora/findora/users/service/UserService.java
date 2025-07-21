package com.findora.findora.users.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.findora.findora.agreement.dto.AgreementRequestDTO;
import com.findora.findora.agreement.model.AgreementType;
import com.findora.findora.agreement.model.UserAgreement;
import com.findora.findora.agreement.repository.UserAgreementRepository;
import com.findora.findora.common.email.EmailSender;
import com.findora.findora.common.SuccessResponse;
import com.findora.findora.emailverification.service.EmailVerificationService;
import com.findora.findora.users.dto.UserRegisterRequestDTO;
import com.findora.findora.users.model.User;
import com.findora.findora.users.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final EmailSender emailSender;
    private final UserAgreementRepository userAgreementRepository;
    
    // 사용자 등록
    @Transactional
    public User registerUser(UserRegisterRequestDTO userRegisterRequestDTO) {
        // 중복 검사 (탈퇴한 사용자 포함)
        if (isEmailUsedIncludingDeleted(userRegisterRequestDTO.getEmail())) {
            throw new IllegalArgumentException("이미 사용된 이메일입니다. (탈퇴한 계정 포함)");
        }
        
        if (isNicknameUsedIncludingDeleted(userRegisterRequestDTO.getNickname())) {
            throw new IllegalArgumentException("이미 사용된 닉네임입니다. (탈퇴한 계정 포함)");
        }
        
        if (isLoginIdUsedIncludingDeleted(userRegisterRequestDTO.getLoginId())) {
            throw new IllegalArgumentException("이미 사용된 로그인 ID입니다. (탈퇴한 계정 포함)");
        }
        
        if (!emailVerificationService.isVerified(userRegisterRequestDTO.getEmail())) {
            throw new IllegalArgumentException("이메일 인증이 필요합니다.");
        }
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userRegisterRequestDTO.getPassword());

        // 사용자 생성 (이메일 인증이 이미 완료된 상태)
        User user = User.builder()
                .loginId(userRegisterRequestDTO.getLoginId())
                .email(userRegisterRequestDTO.getEmail())
                .password(encodedPassword)
                .nickname(userRegisterRequestDTO.getNickname())
                .role(User.Role.valueOf(userRegisterRequestDTO.getRole()))
                .emailVerified(true) // 인증 완료 상태로 저장!
                .build();

        for (AgreementRequestDTO dto : userRegisterRequestDTO.getAgreements()) {
            AgreementType type = AgreementType.valueOf(dto.getType().toUpperCase());
            UserAgreement agreement = UserAgreement.builder()
                    .user(user)
                    .type(type)
                    .agreed(dto.isAgreed())
                    .agreedAt(LocalDateTime.now())
                    .build();
            userAgreementRepository.save(agreement);
        }

        return userRepository.save(user);
    }
    
    // 이메일로 사용자 찾기
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    // ID로 사용자 찾기
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    // 모든 사용자 조회
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    
    // 이메일 인증
    @Transactional
    public void verifyEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        user.verifyEmail();
    }
    
    // 비밀번호 변경
    @Transactional
    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encodedPassword);
    }
    
    // 닉네임 변경
    @Transactional
    public void changeNickname(Long userId, String newNickname) {
        if (isNicknameUsedIncludingDeleted(newNickname)) {
            throw new IllegalArgumentException("이미 사용된 닉네임입니다. (탈퇴한 계정 포함)");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        user.changeNickname(newNickname);
    }
    
    // 로그인
    public User login(String loginId, String password) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return user;
    }
    
    // === 삭제된 사용자까지 포함하는 중복 확인 메서드들 ===
    
    // 닉네임 중복 확인 (탈퇴한 사용자 포함)
    public boolean isNicknameUsedIncludingDeleted(String nickname) {
        return userRepository.countByNicknameIncludingDeleted(nickname) > 0;
    }
    
    // 이메일 중복 확인 (탈퇴한 사용자 포함)
    public boolean isEmailUsedIncludingDeleted(String email) {
        return userRepository.countByEmailIncludingDeleted(email) > 0;
    }
    
    // 로그인ID 중복 확인 (탈퇴한 사용자 포함)
    public boolean isLoginIdUsedIncludingDeleted(String loginId) {
        return userRepository.countByLoginIdIncludingDeleted(loginId) > 0;
    }
    
    // 소프트 삭제 (JPA delete 메서드 사용)
    @Transactional
    public SuccessResponse softDeleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        
        // JPA delete 메서드 호출 시 @SQLDelete 어노테이션으로 인해 
        // 실제로는 UPDATE users SET deleted = true WHERE id = ? 가 실행됨
        userRepository.delete(user);
        
        return SuccessResponse.of("사용자가 성공적으로 삭제되었습니다.");
    }
} 