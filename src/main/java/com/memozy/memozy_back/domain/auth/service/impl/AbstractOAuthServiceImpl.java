package com.memozy.memozy_back.domain.auth.service.impl;

import com.memozy.memozy_back.domain.auth.service.OAuthService;
import com.memozy.memozy_back.domain.user.constant.SocialPlatform;
import com.memozy.memozy_back.domain.user.constant.UserRole;
import com.memozy.memozy_back.domain.user.domain.SocialUserInfo;
import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.domain.user.repository.SocialUserInfoRepository;
import com.memozy.memozy_back.domain.user.repository.UserRepository;

public abstract class AbstractOAuthServiceImpl implements OAuthService {

    protected final SocialUserInfoRepository socialUserInfoRepository;
    protected final UserRepository userRepository;

    protected AbstractOAuthServiceImpl(SocialUserInfoRepository socialUserInfoRepository,
            UserRepository userRepository) {
        this.socialUserInfoRepository = socialUserInfoRepository;
        this.userRepository = userRepository;
    }

    protected User handleSocialLogin(SocialPlatform platform, String socialCode, String email, String username, String profileImageUrl) {
        return socialUserInfoRepository.findBySocialCode(socialCode)
                .map(SocialUserInfo::getUser)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(existingUser -> {
                            if (!socialUserInfoRepository.existsByUserAndSocialType(existingUser, platform)) {
                                socialUserInfoRepository.save(SocialUserInfo.newInstance(existingUser, platform, socialCode));
                            }
                            return existingUser;
                        })
                        .orElseGet(() -> {
                            User newUser = userRepository.save(User.create(UserRole.MEMBER, username, email, profileImageUrl));
                            socialUserInfoRepository.save(SocialUserInfo.newInstance(newUser, platform, socialCode));
                            return newUser;
                        })
                );
    }
}