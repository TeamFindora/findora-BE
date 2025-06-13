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
import com.findora.findora.emailverification.service.EmailVerificationService;
import com.findora.findora.users.dto.UserRegisterRequestDTO;
import com.findora.findora.users.model.User;
import com.findora.findora.users.repository.UserRepository;

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
        // 중복 검사
        if (userRepository.existsByEmail(userRegisterRequestDTO.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        
        if (userRepository.existsByNickname(userRegisterRequestDTO.getNickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
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
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.verifyEmail();
    }
    
    // 비밀번호 변경
    @Transactional
    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encodedPassword);
    }
    
    // 닉네임 변경
    @Transactional
    public void changeNickname(Long userId, String newNickname) {
        if (userRepository.existsByNickname(newNickname)) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.changeNickname(newNickname);
    }
    
    public User login(String loginId, String password) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return user;
    }
} 