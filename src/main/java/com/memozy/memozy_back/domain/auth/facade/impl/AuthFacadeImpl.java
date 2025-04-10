package com.memozy.memozy_back.domain.auth.facade.impl;

import com.memozy.memozy_back.domain.auth.dto.response.SocialLoginResponse;
import com.memozy.memozy_back.domain.auth.dto.response.TokenResponse;
import com.memozy.memozy_back.domain.auth.facade.AuthFacade;
import com.memozy.memozy_back.domain.auth.service.OAuthService;
import com.memozy.memozy_back.domain.auth.service.provider.OAuthServiceProvider;
import com.memozy.memozy_back.domain.user.domain.SocialPlatform;
import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.domain.user.repository.UserRepository;
import com.memozy.memozy_back.global.jwt.JwtProvider;
import com.memozy.memozy_back.global.jwt.TokenInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AuthFacadeImpl implements AuthFacade {

    private final OAuthServiceProvider oAuthServiceProvider;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TokenResponse socialLogin(
            String origin,
            SocialPlatform socialPlatform,
            String authorizationCode) {
        OAuthService oAuthService = oAuthServiceProvider.getService(socialPlatform);
        User user = oAuthService.socialUserLogin(origin, authorizationCode);
        return TokenResponse.from(
                jwtProvider.createTokenCollection(
                        TokenInfo.from(user)));
    }

    @Override
    @Transactional(readOnly = true)
    public SocialLoginResponse getSocialLoginPageUrl(String origin, SocialPlatform socialPlatform) {
        OAuthService oAuthService = oAuthServiceProvider.getService(socialPlatform);
        return SocialLoginResponse.from(oAuthService.getLoginPageUrl(origin));
    }

}