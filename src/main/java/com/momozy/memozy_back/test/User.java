package com.momozy.memozy_back.test;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users") // 'user'는 예약어라 테이블명은 복수형 추천!
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    private String email;
}