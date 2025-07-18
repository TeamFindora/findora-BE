package com.findora.findora.auth.repository;

import com.findora.findora.auth.model.SocialAuth;
import com.findora.findora.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SocialAuthRepository extends JpaRepository<SocialAuth, Long> {
    Optional<SocialAuth> findByProviderAndProviderUserId(String provider, String providerUserId);
    Optional<SocialAuth> findByUser(User user);
} 