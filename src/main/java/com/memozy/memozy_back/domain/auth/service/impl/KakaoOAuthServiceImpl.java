package com.memozy.memozy_back.domain.auth.service.impl;

import com.memozy.memozy_back.domain.auth.service.OAuthService;
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
@RequiredArgsConstructor
public class KakaoOAuthServiceImpl implements OAuthService {

    private final JwtProperty jwtProperty;
//    private final KakaoAuthServerClient kakaoAuthServerClient;
    private final KakaoServerClient kakaoServerClient;
//    private final KakaoClientProperty kakaoClientProperty;
    private final SocialUserInfoRepository socialUserInfoRepository;
    private final UserRepository userRepository;

    @Override
    public boolean support(SocialPlatform socialPlatform) {
        return socialPlatform == SocialPlatform.KAKAO;
    }

    @Override
    @Transactional
    public User socialUserLogin(String kakaoAccessToken, String username) {
        KakaoSocialUserProfile socialUserProfile = kakaoServerClient.getUserInformation(
                jwtProperty.getBearerPrefix() + " " + kakaoAccessToken);

        String socialCode = SocialUserInfo.calculateSocialCode(
                SocialPlatform.KAKAO,
                String.valueOf(socialUserProfile.getId())
        );

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
                                    socialUserProfile.getNickname(),
                                    email,
                                    socialUserProfile.getProfileImageUrl()
                            ));
                            socialUserInfoRepository.save(SocialUserInfo.newInstance(
                                    newUser, SocialPlatform.GOOGLE, socialCode));
                            return newUser;
                        })
                );
    }
}