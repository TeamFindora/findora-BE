package com.findora.findora.users.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.findora.findora.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "users",
       uniqueConstraints = {
         @UniqueConstraint(name = "uk_users_email",    columnNames = "email"),
         @UniqueConstraint(name = "uk_users_nickname", columnNames = "nickname")
       })
@SQLDelete(sql = "UPDATE users SET deleted = true, updated_at = NOW() WHERE id = ?")
@Where(clause = "deleted = false")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class User extends BaseEntity {

    @Column(name = "login_id", nullable = false, length = 50)
    private String loginId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    public enum Role {
        USER, STUDENT, PI, ADMIN, WAITING
    }

    public void changeRole(Role newRole) {
        this.role = newRole;
    }

    // 편의 메서드 예시
    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeNickname(String newNickname) {
        this.nickname = newNickname;
    }
}
