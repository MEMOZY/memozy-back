package com.memozy.memozy_back.domain.alert.dto.request;

import com.memozy.memozy_back.domain.alert.domain.Platform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDeviceTokenRequest(

        @Schema(description = "디바이스 플랫폼 (iOS, Android)", example = "iOS")
        @NotNull
        Platform platform,

        @Schema(description = "Expo Push Notification을 위한 디바이스 토큰")
        @NotBlank
        String deviceToken
) {
}
