package com.memozy.memozy_back.domain.auth.service.impl;

import com.memozy.memozy_back.domain.auth.service.OAuthService;
import com.memozy.memozy_back.domain.file.service.FileService;
import com.memozy.memozy_back.domain.user.constant.SocialPlatform;
import com.memozy.memozy_back.domain.user.domain.SocialUserInfo;
import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.domain.user.constant.UserRole;
import com.memozy.memozy_back.domain.user.repository.SocialUserInfoRepository;
import com.memozy.memozy_back.domain.user.repository.UserRepository;
import com.memozy.memozy_back.global.feign.oauth.kakao.KakaoServerClient;
import com.memozy.memozy_back.global.feign.oauth.kakao.KakaoSocialUserProfile;
import com.memozy.memozy_back.global.jwt.JwtProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class KakaoOAuthServiceImpl extends AbstractOAuthServiceImpl {

    private final JwtProperty jwtProperty;
    private final KakaoServerClient kakaoServerClient;

    public KakaoOAuthServiceImpl(JwtProperty jwtProperty, KakaoServerClient kakaoServerClient,
            SocialUserInfoRepository socialUserInfoRepository, UserRepository userRepository, FileService fileService) {
        super(socialUserInfoRepository, userRepository, fileService);
        this.jwtProperty = jwtProperty;
        this.kakaoServerClient = kakaoServerClient;
    }

    @Override
    public boolean support(SocialPlatform socialPlatform) {
        return socialPlatform == SocialPlatform.KAKAO;
    }

    @Override
    @Transactional
    public User socialUserLogin(String kakaoAccessToken, String ignored) {
        var profile = kakaoServerClient.getUserInformation(jwtProperty.getBearerPrefix() + " " + kakaoAccessToken);
        String socialCode = SocialUserInfo.calculateSocialCode(SocialPlatform.KAKAO, String.valueOf(profile.getId()));
        return handleSocialLogin(SocialPlatform.KAKAO, socialCode, profile.getEmail(), profile.getNickname(), profile.getProfileImageUrl());
    }
}