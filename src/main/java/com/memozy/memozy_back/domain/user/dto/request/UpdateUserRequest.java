package com.memozy.memozy_back.domain.user.dto.request;

import static com.memozy.memozy_back.domain.auth.exception.AuthErrorMessage.EMAIL_IS_NULL;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record UpdateUserRequest(
        @Schema(description = "닉네임", example = "신민규")
        String nickname,

        @Schema(description = "이메일", example = "digi1k@naver.com")
        String email,

        @NotNull
        @Schema(description = "프로필 이미지 URL", example = "기본 이미지는 빈 문자열로 보내주세요")
        String profileImageUrl,

        @Schema(description = "휴대번호", example = "010-1234-5678")
        String phoneNumber
) {
}
