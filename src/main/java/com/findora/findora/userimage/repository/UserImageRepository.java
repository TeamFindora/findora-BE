package com.findora.findora.userimage.repository;

import com.findora.findora.userimage.model.UserImage;
import com.findora.findora.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserImageRepository extends JpaRepository<UserImage, Long> {
    List<UserImage> findByStatus(UserImage.VerificationStatus status);

    // 사용자 ID로 UserImage 조회
    Optional<UserImage> findByUserId(Long userId);


}
