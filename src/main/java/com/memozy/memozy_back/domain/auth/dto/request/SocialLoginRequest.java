package com.memozy.memozy_back.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record SocialLoginRequest(
        @Schema(example = "Kakao/Google - AccessToken, Apple - ID Token")
        @NotNull String token,
        @Schema(description = "최초 로그인 시에만") String name
) {
}