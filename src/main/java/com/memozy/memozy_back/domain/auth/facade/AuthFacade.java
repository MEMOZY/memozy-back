package com.memozy.memozy_back.domain.auth.facade;

import com.memozy.memozy_back.domain.auth.dto.response.TokenResponse;
import com.memozy.memozy_back.domain.user.constant.SocialPlatform;

public interface AuthFacade {
    TokenResponse socialLogin(
//            String origin,
            SocialPlatform socialPlatform,
            String authorizationCode);

    // 앱 환경에선 필요없음
//    SocialLoginResponse getSocialLoginPageUrl(String origin, SocialPlatform socialPlatform);

    TokenResponse reissue(String refreshToken);


}
