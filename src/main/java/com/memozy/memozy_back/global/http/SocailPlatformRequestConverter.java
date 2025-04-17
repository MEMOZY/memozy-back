package com.memozy.memozy_back.global.http;

import com.memozy.memozy_back.domain.user.constant.SocialPlatform;
import org.springframework.core.convert.converter.Converter;

public class SocailPlatformRequestConverter implements Converter<String, SocialPlatform> {

    @Override
    public SocialPlatform convert(String socialPlatform) {
        return SocialPlatform.of(socialPlatform);
    }
}