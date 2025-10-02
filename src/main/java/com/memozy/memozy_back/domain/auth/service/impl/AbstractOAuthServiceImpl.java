package com.memozy.memozy_back.domain.auth.service.impl;

import com.memozy.memozy_back.domain.auth.service.OAuthService;
import com.memozy.memozy_back.domain.file.service.FileService;
import com.memozy.memozy_back.domain.user.constant.SocialPlatform;
import com.memozy.memozy_back.domain.user.constant.UserRole;
import com.memozy.memozy_back.domain.user.domain.SocialUserInfo;
import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.domain.user.repository.SocialUserInfoRepository;
import com.memozy.memozy_back.domain.user.repository.UserRepository;
import com.memozy.memozy_back.global.exception.GlobalException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import java.util.Optional;

public abstract class AbstractOAuthServiceImpl implements OAuthService {

    protected final SocialUserInfoRepository socialUserInfoRepository;
    protected final UserRepository userRepository;
    protected final FileService fileService;

    protected AbstractOAuthServiceImpl(SocialUserInfoRepository socialUserInfoRepository,
            UserRepository userRepository, FileService fileService) {
        this.socialUserInfoRepository = socialUserInfoRepository;
        this.userRepository = userRepository;
        this.fileService = fileService;
    }


    protected User handleSocialLogin(
            SocialPlatform platform,
            String socialCode,
            String email,
            String username,
            String profileImageUrl
    ) {
        if (username == null || username.isBlank()) {
            throw new GlobalException(ErrorCode.AUTH_MISSING_NAME);
        }

        // 1. 동일한 소셜 계정(socialCode)이 이미 연결돼 있다면 해당 유저로 로그인
        Optional<SocialUserInfo> existingInfo = socialUserInfoRepository.findBySocialCode(socialCode);
        if (existingInfo.isPresent()) {
            return existingInfo.get().getUser();
        }

        // 2. socialCode는 처음인데, 같은 이메일을 가진 유저가 있다면 그 유저에 연결
        User user = null;
        if (email != null && !email.isBlank()) {
            user = userRepository.findByEmail(email).orElse(null);
        }

        // 3. 유저 없으면 새로 생성
        if (user == null) {
            if (!profileImageUrl.isBlank()) {
                profileImageUrl = fileService.saveImageToS3(profileImageUrl);
            }
            user = userRepository.save(User.create(UserRole.MEMBER, username, email, profileImageUrl));
        }

        // 4. 소셜 계정 연동
        socialUserInfoRepository.save(SocialUserInfo.newInstance(user, platform, socialCode));
        return user;
    }
}