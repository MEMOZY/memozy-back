package com.memozy.memozy_back.domain.auth.dto.response;

import com.memozy.memozy_back.domain.user.domain.User;

public record TestTokenResponse(
        Long userId,
        String accessToken,
        String refreshToken
) {
    public static TestTokenResponse from(User user, TokenResponse tokenResponse) {
        return new TestTokenResponse(user.getId(), tokenResponse.accessToken(), tokenResponse.refreshToken());
    }
}
