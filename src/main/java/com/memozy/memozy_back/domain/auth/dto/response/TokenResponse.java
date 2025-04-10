package com.memozy.memozy_back.domain.auth.dto.response;

import com.memozy.memozy_back.global.jwt.TokenCollection;
import jakarta.validation.constraints.NotNull;

public record TokenResponse(
        @NotNull String accessToken,
        @NotNull String refreshToken) {

    public static TokenResponse from(TokenCollection tokenCollection) {
        return new TokenResponse(
                tokenCollection.getAccessToken(),
                tokenCollection.getRefreshToken());
    }
}

