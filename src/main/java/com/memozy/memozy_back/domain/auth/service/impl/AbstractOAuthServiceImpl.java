package com.memozy.memozy_back.domain.auth.service.impl;

import com.memozy.memozy_back.domain.auth.service.OAuthService;
import com.memozy.memozy_back.domain.user.constant.SocialPlatform;
import com.memozy.memozy_back.domain.user.constant.UserRole;
import com.memozy.memozy_back.domain.user.domain.SocialUserInfo;
import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.domain.user.repository.SocialUserInfoRepository;
import com.memozy.memozy_back.domain.user.repository.UserRepository;
import com.memozy.memozy_back.global.exception.BusinessException;
import com.memozy.memozy_back.global.exception.ErrorCode;

public abstract class AbstractOAuthServiceImpl implements OAuthService {

    protected final SocialUserInfoRepository socialUserInfoRepository;
    protected final UserRepository userRepository;

    protected AbstractOAuthServiceImpl(SocialUserInfoRepository socialUserInfoRepository,
            UserRepository userRepository) {
        this.socialUserInfoRepository = socialUserInfoRepository;
        this.userRepository = userRepository;
    }

    protected User handleSocialLogin(
            SocialPlatform platform,
            String socialCode,
            String email,
            String username,
            String profileImageUrl
    ) {
        if (username == null || username.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_MISSING_NAME);
        }
        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_MISSING_EMAIL);
        }

        // 1. 이메일로 유저를 먼저 조회
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 없으면 새 유저 생성
                    return userRepository.save(User.create(UserRole.MEMBER, username, email, profileImageUrl));
                });

        // 2. 해당 유저가 이 플랫폼으로 이미 연동됐는지 확인
        boolean alreadyLinked = socialUserInfoRepository.existsByUserAndSocialType(user, platform);
        if (!alreadyLinked) {
            // 연동 안 돼 있으면 연동 추가
            socialUserInfoRepository.save(SocialUserInfo.newInstance(user, platform, socialCode));
        }

        return user;
    }
}