package com.findora.findora.emailverification.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.findora.findora.emailverification.model.EmailVerification;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByEmail(String email);
    boolean existsByEmailAndVerifiedTrue(String email);
    Optional<EmailVerification> findByCode(String code);
}