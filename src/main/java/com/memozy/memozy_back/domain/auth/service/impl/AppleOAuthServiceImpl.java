package com.memozy.memozy_back.domain.auth.service.impl;

import com.memozy.memozy_back.domain.auth.service.OAuthService;
import com.memozy.memozy_back.domain.auth.service.provider.ApplePublicKeyProvider;
import com.memozy.memozy_back.domain.user.constant.SocialPlatform;
import com.memozy.memozy_back.domain.user.constant.UserRole;
import com.memozy.memozy_back.domain.user.domain.SocialUserInfo;
import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.domain.user.repository.SocialUserInfoRepository;
import com.memozy.memozy_back.domain.user.repository.UserRepository;
import com.memozy.memozy_back.global.exception.BusinessException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class AppleOAuthServiceImpl extends AbstractOAuthServiceImpl {

    private final ApplePublicKeyProvider applePublicKeyProvider; // Apple 공개키 관리용

    public AppleOAuthServiceImpl(SocialUserInfoRepository socialUserInfoRepository, UserRepository userRepository) {
        super(socialUserInfoRepository, userRepository);
        this.applePublicKeyProvider = new ApplePublicKeyProvider();
    }

    @Override
    public boolean support(SocialPlatform socialPlatform) {
        return socialPlatform == SocialPlatform.APPLE;
    }

    @Override
    @Transactional
    public User socialUserLogin(String idToken, String username) {
        ApplePublicKeyProvider.Payload payload = applePublicKeyProvider.parseAndValidate(idToken);
        String email = payload.email();
        String socialCode = SocialUserInfo.calculateSocialCode(SocialPlatform.APPLE, payload.sub());

        if (username == null || username.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_MISSING_NAME);
        }
        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_MISSING_EMAIL);
        }

        return handleSocialLogin(SocialPlatform.APPLE, socialCode, email, username, null);
    }
}
