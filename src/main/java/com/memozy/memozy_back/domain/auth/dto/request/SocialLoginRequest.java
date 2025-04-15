package com.memozy.memozy_back.domain.auth.dto.request;

import jakarta.validation.constraints.NotNull;

public record SocialLoginRequest(
        @NotNull String code
) {
}
