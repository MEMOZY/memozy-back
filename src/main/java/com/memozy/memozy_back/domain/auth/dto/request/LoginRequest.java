package com.memozy.memozy_back.domain.auth.dto.request;

import static com.memozy.memozy_back.domain.auth.exception.AuthErrorMessage.EMAIL_IS_NULL;
import static com.memozy.memozy_back.domain.auth.exception.AuthErrorMessage.PASSWORD_IS_NULL;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청")
public record LoginRequest(
        @Schema(description = "이메일", example = "digi1k@naver.com")
        @NotBlank(message = EMAIL_IS_NULL)
        String email,
        @Schema(description = "비밀번호", example = "memozy123!")
        @NotBlank(message = PASSWORD_IS_NULL)
        String password
) {

}