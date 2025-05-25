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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleOAuthServiceImpl implements OAuthService {

    private final SocialUserInfoRepository socialUserInfoRepository;
    private final UserRepository userRepository;
    private final ApplePublicKeyProvider applePublicKeyProvider; // Apple 공개키 관리용

    @Override
    public boolean support(SocialPlatform socialPlatform) {
        return socialPlatform == SocialPlatform.APPLE;
    }

    @Override
    @Transactional
    public User socialUserLogin(String idToken, String username) {
        ApplePublicKeyProvider.Payload payload = applePublicKeyProvider.parseAndValidate(idToken);

        String socialCode = SocialUserInfo.calculateSocialCode(
                SocialPlatform.APPLE,
                payload.sub()
        );

        String email = payload.email();

        // 이미 등록된 Apple 사용자라면 name, email 없이 OK
        return socialUserInfoRepository.findBySocialCode(socialCode)
                .map(SocialUserInfo::getUser)
                .orElseGet(() -> {
                    // 최초 로그인인데 name, email이 null이면 에러
                    if (username == null || username.isBlank()) {
                        throw new BusinessException(ErrorCode.APPLE_MISSING_NAME);
                    }

                    User newUser = userRepository.save(
                            User.create(
                                    UserRole.MEMBER,
                                    username,
                                    email,
                                    null  // Apple은 기본 프로필 이미지 없음
                            )
                    );

                    socialUserInfoRepository.save(
                            SocialUserInfo.newInstance(
                                    newUser,
                                    SocialPlatform.APPLE,
                                    socialCode
                            )
                    );

                    return newUser;
                });
    }
}
