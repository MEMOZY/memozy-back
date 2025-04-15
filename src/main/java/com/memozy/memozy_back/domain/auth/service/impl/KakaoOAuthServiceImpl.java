package com.memozy.memozy_back.domain.auth.service.impl;

import com.memozy.memozy_back.domain.auth.service.OAuthService;
import com.memozy.memozy_back.domain.user.domain.SocialPlatform;
import com.memozy.memozy_back.domain.user.domain.SocialUserInfo;
import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.domain.user.domain.UserRole;
import com.memozy.memozy_back.domain.user.repository.SocialUserInfoRepository;
import com.memozy.memozy_back.domain.user.repository.UserRepository;
import com.memozy.memozy_back.global.feign.OAuthToken;
import com.memozy.memozy_back.global.feign.oauth.kakao.KakaoAuthServerClient;
import com.memozy.memozy_back.global.feign.oauth.kakao.KakaoClientProperty;
import com.memozy.memozy_back.global.feign.oauth.kakao.KakaoServerClient;
import com.memozy.memozy_back.global.feign.oauth.kakao.KakaoSocialUserProfile;
import com.memozy.memozy_back.global.jwt.JwtProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class KakaoOAuthServiceImpl implements OAuthService {

    private final JwtProperty jwtProperty;
    private final KakaoAuthServerClient kakaoAuthServerClient;
    private final KakaoServerClient kakaoServerClient;
    private final KakaoClientProperty kakaoClientProperty;
    private final SocialUserInfoRepository socialUserInfoRepository;
    private final UserRepository userRepository;

    @Override
    public boolean support(SocialPlatform socialPlatform) {
        return socialPlatform == SocialPlatform.KAKAO;
    }

    @Override
    @Transactional
    public User socialUserLogin(String origin, String authorizationCode) {
        OAuthToken oAuthToken = kakaoAuthServerClient.getOAuth2AccessToken(
                kakaoClientProperty.getContentType(),
                kakaoClientProperty.getGrantType(),
                kakaoClientProperty.getClientId(),
                origin + kakaoClientProperty.getRedirectPath(),
                authorizationCode);

        KakaoSocialUserProfile socialUserProfile = kakaoServerClient.getUserInformation(
                jwtProperty.getBearerPrefix() + oAuthToken.getAccessToken());

        String socialCode = SocialUserInfo.calculateSocialCode(
                SocialPlatform.KAKAO,
                String.valueOf(socialUserProfile.getId()));

        return socialUserInfoRepository
                .findBySocialCode(socialCode)
                .map(SocialUserInfo::getUser)
                .orElseGet(() -> {
                    // 회원가입
                    User newUser = userRepository.save(
                            User.from(UserRole.MEMBER));

                    socialUserInfoRepository.save(
                            SocialUserInfo.newInstance(
                                    newUser,
                                    SocialPlatform.KAKAO,
                                    socialCode));
                    return newUser;
                });
    }

    @Override
    public String getLoginPageUrl(String origin) {
        return UriComponentsBuilder
                .fromHttpUrl(kakaoClientProperty.getLoginPageUrl())
                .queryParam("client_id", kakaoClientProperty.getClientId())
                .queryParam("redirect_uri", origin + kakaoClientProperty.getRedirectPath())
                .build()
                .toUriString();
    }
}