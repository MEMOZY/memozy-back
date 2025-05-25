package com.memozy.memozy_back.domain.auth.service;

import com.memozy.memozy_back.domain.user.constant.SocialPlatform;
import com.memozy.memozy_back.domain.user.domain.User;

public interface OAuthService {

    boolean support(SocialPlatform socialPlatform);

    User socialUserLogin(String idToken, String username);

//    String getLoginPageUrl(String origin);
}