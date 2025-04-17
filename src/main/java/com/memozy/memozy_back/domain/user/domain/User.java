package com.memozy.memozy_back.domain.user.domain;

import com.memozy.memozy_back.domain.user.constant.UserRole;
import com.memozy.memozy_back.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@Builder
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
//@Where(clause = "is_deleted = false")
//@SQLDelete(sql = "UPDATE user SET is_deleted = true WHERE user_id = ?")
public class User extends BaseTimeEntity {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false, length = 10)
    @ColumnDefault(value = "'MEMBER'")
    private UserRole userRole;

    @Column(length = 2048)
    private String profileImageUrl;

    @Column(length = 100)
    private String username;

    @Column(length = 100)
    private String nickname;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 2048)
    private String email;

    public static User from(UserRole userRole, String nickname, String profileImageUrl) {
        return User.builder()
                .userRole(userRole)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .build();
    }


}
