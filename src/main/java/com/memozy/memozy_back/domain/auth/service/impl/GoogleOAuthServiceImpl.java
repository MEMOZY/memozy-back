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
import com.memozy.memozy_back.global.feign.oauth.google.GoogleServerClient;
import com.memozy.memozy_back.global.feign.oauth.google.GoogleSocialUserProfile;
import com.memozy.memozy_back.global.jwt.JwtProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOAuthServiceImpl implements OAuthService {

    private final JwtProperty jwtProperty;
//    private final GoogleAuthServerClient googleAuthServerClient;
    private final GoogleServerClient googleServerClient;
//    private final GoogleClientProperty googleClientProperty;
    private final SocialUserInfoRepository socialUserInfoRepository;
    private final UserRepository userRepository;

    @Override
    public boolean support(SocialPlatform socialPlatform) {
        return socialPlatform == SocialPlatform.GOOGLE;
    }

    @Override
    @Transactional
    public User socialUserLogin(String googleAccessToken, String username) {
        GoogleSocialUserProfile socialUserProfile = googleServerClient.getUserInformation(
                jwtProperty.getBearerPrefix() + " " + googleAccessToken);
        log.info("📦 GoogleSocialUserProfile: {}", socialUserProfile);

        String socialCode = SocialUserInfo.calculateSocialCode(SocialPlatform.GOOGLE, socialUserProfile.getSub());
        String email = socialUserProfile.getEmail();

        return socialUserInfoRepository
                .findBySocialCode(socialCode)
                .map(SocialUserInfo::getUser)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(existingUser -> {
                            boolean exists = socialUserInfoRepository.existsByUserAndSocialType(existingUser, SocialPlatform.GOOGLE);
                            if (!exists) {
                                socialUserInfoRepository.save(SocialUserInfo.newInstance(existingUser, SocialPlatform.GOOGLE, socialCode));
                            }
                            return existingUser;
                        })
                        .orElseGet(() -> {
                            User newUser = userRepository.save(User.create(
                                    UserRole.MEMBER,
                                    socialUserProfile.getName(),
                                    email,
                                    socialUserProfile.getPicture()
                            ));
                            socialUserInfoRepository.save(SocialUserInfo.newInstance(
                                    newUser, SocialPlatform.GOOGLE, socialCode));
                            return newUser;
                        })
                );
    }



//    @Override
//    public String getLoginPageUrl(String origin) {
//        return new StringBuilder()
//                .append(googleClientProperty.getLoginPageUrl())
//                .append("&client_id=")
//                .append(googleClientProperty.getClientId())
//                .append("&redirect_uri=")
//                .append(origin)
//                .append(googleClientProperty.getRedirectPath())
//                .toString();
//    }
}
