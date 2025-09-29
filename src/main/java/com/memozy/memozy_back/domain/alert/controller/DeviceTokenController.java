package com.memozy.memozy_back.domain.alert.controller;

import com.memozy.memozy_back.domain.alert.dto.request.CreateDeviceTokenRequest;
import com.memozy.memozy_back.domain.alert.service.DeviceTokenService;
import com.memozy.memozy_back.global.annotation.CurrentUserId;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Device Token API", description = "푸쉬 알람을 위한 디바이스 토큰 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/device-tokens")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    @PostMapping()
    public ResponseEntity<Void> registerDeviceToken(
            @CurrentUserId Long userId,
            @Valid @RequestBody CreateDeviceTokenRequest request) {
        deviceTokenService.registerDeviceToken(userId, request);
        return ResponseEntity.ok().build();
    }

}