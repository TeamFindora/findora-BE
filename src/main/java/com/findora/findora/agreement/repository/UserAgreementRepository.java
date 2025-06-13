package com.findora.findora.agreement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.findora.findora.agreement.model.AgreementType;
import com.findora.findora.agreement.model.UserAgreement;
import com.findora.findora.users.model.User;

public interface UserAgreementRepository extends JpaRepository<UserAgreement, Long> {
    // 특정 유저의 모든 약관 동의 이력 조회
    List<UserAgreement> findByUser(User user);

    // 특정 유저가 특정 약관에 동의했는지 조회
    Optional<UserAgreement> findByUserAndType(User user, AgreementType type);

    // 특정 유저가 특정 약관에 동의했는지 boolean으로
    boolean existsByUserAndTypeAndAgreedTrue(User user, AgreementType type);

    // (선택) 약관 종류별 전체 동의자 수
    long countByTypeAndAgreedTrue(AgreementType type);
}