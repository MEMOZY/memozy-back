package com.memozy.memozy_back.domain.auth.service.provider;

import com.memozy.memozy_back.domain.auth.service.OAuthService;
import com.memozy.memozy_back.domain.user.domain.SocialPlatform;
import com.memozy.memozy_back.global.exception.BusinessException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthServiceProvider {

    private final List<OAuthService> socialServices;

    public OAuthService getService(SocialPlatform socialPlatform) {
        for (OAuthService oAuthService : socialServices) {
            if (oAuthService.support(socialPlatform)) {
                return oAuthService;
            }
        }
        throw new BusinessException(ErrorCode.UNSUPPORTED_SOCIAL_PLATFORM_EXCEPTION);
    }
}