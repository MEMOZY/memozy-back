package com.memozy.memozy_back.domain.user.dto.request;

import static com.memozy.memozy_back.domain.auth.exception.AuthErrorMessage.EMAIL_IS_NULL;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;


public record UpdateUserRequest(
        @Schema(description = "이름", example = "digi1k@naver.com")
        String username,

        @Schema(description = "닉네임", example = "신민규")
        String nickname,

        @Schema(description = "이메일", example = "digi1k@naver.com")
        String email,
        String profileImageUrl,

        @Schema(description = "휴대번호", example = "010-1234-5678")
        String phoneNumber
) {
}
