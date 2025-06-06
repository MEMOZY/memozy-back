package com.memozy.memozy_back.domain.user.constant;

import java.util.Arrays;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SocialPlatform {

    KAKAO("kakao"),
    GOOGLE("google"),
    APPLE("apple"),
    ;

    private final String name;

    public static SocialPlatform of(String name) {
        return Arrays.stream(SocialPlatform.values())
                .filter(socialPlatform -> socialPlatform.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 소셜 플랫폼입니다."));
    }
}