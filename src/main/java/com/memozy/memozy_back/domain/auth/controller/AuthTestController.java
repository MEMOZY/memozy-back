package com.memozy.memozy_back.domain.auth.controller;

import com.memozy.memozy_back.domain.auth.dto.response.TestTokenResponse;
import com.memozy.memozy_back.domain.auth.dto.response.TokenResponse;
import com.memozy.memozy_back.domain.user.constant.UserRole;
import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.domain.user.repository.UserRepository;
import com.memozy.memozy_back.global.exception.BusinessException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import com.memozy.memozy_back.global.jwt.JwtProvider;
import com.memozy.memozy_back.global.jwt.TokenInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "테스트 토큰 발급 API", description = "로컬 개발용 토큰 발급")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthTestController {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @PostMapping("/test-token")
    public ResponseEntity<TestTokenResponse> generateTestToken(
            @RequestParam(name = "userId", required = false) Long userId
    ) {
        User user = userId != null
                ? userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER_EXCEPTION))
                : userRepository.save(User.from(UserRole.MEMBER, "test-user", "https://example.com/image.jpg"));

        TokenResponse tokenResponse = TokenResponse.from(
                jwtProvider.createTokenCollection(
                        TokenInfo.from(user)));

        return ResponseEntity.ok(TestTokenResponse.from(user, tokenResponse));
    }
}