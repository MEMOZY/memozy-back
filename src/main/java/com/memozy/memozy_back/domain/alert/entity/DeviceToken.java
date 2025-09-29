package com.memozy.memozy_back.domain.alert.entity;

import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.global.entity.BaseEntity;
import com.memozy.memozy_back.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(
        name = "device_tokens",
        indexes = {
                @Index(name = "idx_device_tokens_user_id", columnList = "user_id"),
                @Index(name = "idx_device_tokens_valid", columnList = "is_valid")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_device_tokens_expo_token", columnNames = "expo_token")
        }
)
@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DeviceToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expo_token", nullable = false)
    private String expoToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Platform platform;

    @Builder.Default
    @Column(name = "is_valid", nullable = false)
    private Boolean isValid = true;

    public static DeviceToken create(User user, String expoToken, Platform platform) {
        return DeviceToken.builder()
                .user(user)
                .expoToken(expoToken)
                .platform(platform)
                .isValid(true)
                .build();
    }
}