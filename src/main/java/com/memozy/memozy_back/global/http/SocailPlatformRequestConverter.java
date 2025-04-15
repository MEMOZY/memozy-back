package com.memozy.memozy_back.global.http;

import com.memozy.memozy_back.domain.user.domain.SocialPlatform;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;

public class SocailPlatformRequestConverter implements Converter<String, SocialPlatform> {

    @Override
    public SocialPlatform convert(String socialPlatform) {
        return SocialPlatform.of(socialPlatform);
    }
}