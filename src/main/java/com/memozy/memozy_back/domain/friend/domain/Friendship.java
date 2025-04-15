package com.memozy.memozy_back.domain.friend.domain;

import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Friendship extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status; // REQUESTED, ACCEPTED, REJECTED 등


    public static Friendship create(User sender, User receiver) {
        return Friendship.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendshipStatus.REQUESTED)
                .build();
    }

    public void accept() {
        if (this.status != FriendshipStatus.REQUESTED) {
            throw new IllegalStateException("요청받은 상태가 아닙니다");
        }
        this.status = FriendshipStatus.ACCEPTED;
    }
}