package com.memozy.memozy_back.domain.auth.controller;

import com.memozy.memozy_back.domain.auth.dto.request.ReissueTokenRequest;
import com.memozy.memozy_back.domain.auth.dto.request.SocialLoginRequest;
import com.memozy.memozy_back.domain.auth.dto.response.TokenResponse;
import com.memozy.memozy_back.domain.auth.facade.AuthFacade;
import com.memozy.memozy_back.domain.user.constant.SocialPlatform;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증 관련 API", description = "소셜 로그인")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

//    private final AuthService authService;
    private final AuthFacade authFacade;

//    @Override
//    @PostMapping("/login")
//    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
//        LoginResponse response = authService.login(request);
//
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("/social/{socialPlatform}/login")
    public ResponseEntity<TokenResponse> socialLogin(
            @PathVariable(name = "socialPlatform") SocialPlatform socialPlatform,
            @Valid @RequestBody SocialLoginRequest request
    ) {
        TokenResponse response = authFacade.socialLogin(
                socialPlatform,
                request.socialAccessToken());
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/social/{socialPlatform}/login-page")
//    public ResponseEntity<SocialLoginResponse> getSocialLoginPageUrl(
//            @RequestHeader("Origin") String origin,
//            @PathVariable(name = "socialPlatform") SocialPlatform socialPlatform
//    ) {
//        SocialLoginResponse response =
//                authFacade.getSocialLoginPageUrl(origin, socialPlatform);
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@RequestBody @Valid ReissueTokenRequest request) {
        TokenResponse response = authFacade.reissue(request.refreshToken());

        return ResponseEntity.ok(response);
    }
}
