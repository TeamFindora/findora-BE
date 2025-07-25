package com.findora.findora.messageauth.model;

import com.findora.findora.users.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "messageAuth")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "sent_count",nullable = false)
    private int count;

    public MessageAuth(User user, int count) {
        this.user = user;
        this.count = count;
    }

    //sent_count를 감소시키는 메소드
    public void decreaseCount() {
        if (count <= 0) {
            throw new IllegalStateException("남은 쪽지 횟수가 없습니다.");
        }
        this.count--;
    }

    //추가 구매를 위한 메소드(예: 쪽지 횟수 증가)
    public void increaseCount(int amount) {
        this.count += amount;
    }
}