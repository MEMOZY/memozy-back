package com.memozy.memozy_back.domain.auth.dto.response;

import jakarta.validation.constraints.NotNull;

public record SocialLoginResponse(
        @NotNull String loginPageUrl) {

    public static SocialLoginResponse from(String loginPageUrl) {
        return new SocialLoginResponse(loginPageUrl);
    }
}