package com.memozy.memozy_back.domain.auth.service.impl;

import com.memozy.memozy_back.domain.file.service.FileService;
import com.memozy.memozy_back.domain.user.constant.SocialPlatform;
import com.memozy.memozy_back.domain.user.domain.SocialUserInfo;
import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.domain.user.repository.SocialUserInfoRepository;
import com.memozy.memozy_back.domain.user.repository.UserRepository;
import com.memozy.memozy_back.global.feign.oauth.google.GoogleServerClient;
import com.memozy.memozy_back.global.feign.oauth.google.GoogleSocialUserProfile;
import com.memozy.memozy_back.global.jwt.JwtProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class GoogleOAuthServiceImpl extends AbstractOAuthServiceImpl {

    private final JwtProperty jwtProperty;
    private final GoogleServerClient googleServerClient;

    public GoogleOAuthServiceImpl(JwtProperty jwtProperty,
            GoogleServerClient googleServerClient,
            SocialUserInfoRepository socialUserInfoRepository,
            UserRepository userRepository,
            FileService fileService) {
        super(socialUserInfoRepository, userRepository, fileService);
        this.jwtProperty = jwtProperty;
        this.googleServerClient = googleServerClient;
    }

    @Override
    public boolean support(SocialPlatform socialPlatform) {
        return socialPlatform == SocialPlatform.GOOGLE;
    }

    @Override
    @Transactional
    public User socialUserLogin(String googleAccessToken, String ignored) {
        GoogleSocialUserProfile profile = googleServerClient.getUserInformation(
                jwtProperty.getBearerPrefix() + " " + googleAccessToken);
        log.info("📦 GoogleSocialUserProfile: {}", profile);

        String socialCode = SocialUserInfo.calculateSocialCode(SocialPlatform.GOOGLE, profile.getSub());
        return handleSocialLogin(SocialPlatform.GOOGLE, socialCode, profile.getEmail(), profile.getName(), profile.getPicture());
    }
}
