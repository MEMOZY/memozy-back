//package com.momozy.memozy_back.domain.auth.service.impl;
//
//import com.momozy.memozy_back.domain.auth.service.OAuthService;
//import com.momozy.memozy_back.domain.user.domain.SocialPlatform;
//import com.momozy.memozy_back.domain.user.domain.User;
//import com.momozy.memozy_back.domain.user.repository.UserRepository;
//import com.momozy.memozy_back.global.jwt.JwtProperty;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//@Component
//@RequiredArgsConstructor
//public class GoogleOAuthServiceImpl implements OAuthService {
//
//    private final JwtProperty jwtProperty;
//    private final GoogleAuthServerClient googleAuthServerClient;
//    private final GoogleServerClient googleServerClient;
//    private final GoogleClientProperty googleClientProperty;
//    private final SocialUserInfoRepository socialUserInfoRepository;
//    private final UserRepository userRepository;
//
//    @Override
//    public boolean support(SocialPlatform socialPlatform) {
//        return socialPlatform == SocialPlatform.GOOGLE;
//    }
//
//    @Override
//    @Transactional
//    public User socialUserLogin(String origin, String authorizationCode) {
//
//        OAuthToken oAuthToken = googleAuthServerClient.getOAuth2AccessToken(
//                googleClientProperty.getContentType(),
//                googleClientProperty.getGrantType(),
//                googleClientProperty.getClientId(),
//                googleClientProperty.getClientSecret(),
//                origin + googleClientProperty.getRedirectPath(),
//                authorizationCode);
//
//        GoogleSocialUserProfile socialUserProfile = googleServerClient.getUserInformation(
//                jwtProperty.getBearerPrefix() + oAuthToken.getAccessToken());
//
//        String socialCode = SocialUserInfo.calculateSocialCode(
//                SocialPlatform.GOOGLE,
//                socialUserProfile.getSub());
//
//        return socialUserInfoRepository
//                .findBySocialCode(socialCode)
//                .map(SocialUserInfo::getUser)
//                .orElseGet(() -> {
//                    User newUser = userRepository.save(
//                            User.from(UserRole.NON_MEMBER));
//
//                    return socialUserInfoRepository.save(
//                                    SocialUserInfo.newInstance(
//                                            newUser,
//                                            SocialPlatform.GOOGLE,
//                                            socialCode))
//                            .getUser();
//                });
//    }
//
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
//}
