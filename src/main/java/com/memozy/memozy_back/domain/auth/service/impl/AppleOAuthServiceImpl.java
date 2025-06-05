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
import java.util.Optional;
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
        String socialCode = SocialUserInfo.calculateSocialCode(SocialPlatform.APPLE, payload.sub());
        String email = payload.email();

        // 소셜 코드가 등록되어 있다면 해당 유저 반환
        return socialUserInfoRepository.findBySocialCode(socialCode)
                .map(SocialUserInfo::getUser)
                .orElseGet(() -> {
                    // 이름, 이메일 없으면 예외
                    if (username == null || username.isBlank()) {
                        throw new BusinessException(ErrorCode.AUTH_MISSING_NAME);
                    }
                    if (email == null || email.isBlank()) {
                        throw new BusinessException(ErrorCode.AUTH_MISSING_EMAIL);
                    }

                    // 이메일로 기존 유저가 있는 경우 소셜 정보만 추가
                    return userRepository.findByEmail(email)
                            .map(existingUser -> {
                                boolean exists = socialUserInfoRepository.existsByUserAndSocialType(existingUser, SocialPlatform.APPLE);
                                if (!exists) {
                                    socialUserInfoRepository.save(SocialUserInfo.newInstance(existingUser, SocialPlatform.APPLE, socialCode));
                                }
                                return existingUser;
                            })
                            .orElseGet(() -> {
                                // 새 유저 생성 및 소셜 정보 등록
                                User newUser = userRepository.save(User.create(UserRole.MEMBER, username, email, null));
                                socialUserInfoRepository.save(SocialUserInfo.newInstance(newUser, SocialPlatform.APPLE, socialCode));
                                return newUser;
                            });
                });
    }
}
