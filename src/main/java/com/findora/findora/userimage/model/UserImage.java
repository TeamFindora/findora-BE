package com.findora.findora.userimage.model;

import com.findora.findora.users.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status;

    @Column(name = "reject_reason")
    private String rejectReason; // 거절 시 사유(ex."이미지가 흐릿합니다.")

    public enum VerificationStatus {
        PENDING, APPROVED, REJECTED // 대기 중, 승인됨, 거절됨
    }

    //승인
    public void approve() {
        this.status = VerificationStatus.APPROVED;
    }

    //거절
    public void reject(String reason) {
        this.status = VerificationStatus.REJECTED;
        this.rejectReason = reason;
    }

    @Builder
    public UserImage(User user, String imageUrl) {
        this.user = user;
        this.imageUrl = imageUrl;
        this.status = VerificationStatus.PENDING;
    }

}
