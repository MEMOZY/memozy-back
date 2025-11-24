package com.memozy.memozy_back.domain.user.constant;

import com.memozy.memozy_back.global.exception.ErrorCode;
import com.memozy.memozy_back.global.exception.GlobalException;
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
                .orElseThrow(() -> new GlobalException(ErrorCode.UNSUPPORTED_SOCIAL_PLATFORM_EXCEPTION));
    }
}