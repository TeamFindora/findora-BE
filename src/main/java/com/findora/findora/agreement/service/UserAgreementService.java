package com.findora.findora.agreement.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.findora.findora.agreement.model.AgreementType;
import com.findora.findora.agreement.model.UserAgreement;
import com.findora.findora.agreement.repository.UserAgreementRepository;
import com.findora.findora.users.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAgreementService {

    private final UserAgreementRepository userAgreementRepository;

    // 약관 동의 저장
    @Transactional
    public void saveAgreements(User user, List<AgreementType> agreedTypes) {
        for (AgreementType type : agreedTypes) {
            UserAgreement agreement = UserAgreement.builder()
                    .user(user)
                    .type(type)
                    .agreed(true)
                    .agreedAt(LocalDateTime.now())
                    .build();
            userAgreementRepository.save(agreement);
        }
    }

    // 특정 유저의 동의 이력 조회
    public List<UserAgreement> getUserAgreements(User user) {
        return userAgreementRepository.findByUser(user);
    }

    // 특정 유저가 특정 약관에 동의했는지 확인
    public boolean hasAgreed(User user, AgreementType type) {
        return userAgreementRepository.existsByUserAndTypeAndAgreedTrue(user, type);
    }
}