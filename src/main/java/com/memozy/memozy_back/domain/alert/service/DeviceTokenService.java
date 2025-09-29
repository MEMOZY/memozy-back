package com.memozy.memozy_back.domain.alert.service;

import com.memozy.memozy_back.domain.alert.domain.DeviceToken;
import com.memozy.memozy_back.domain.alert.dto.request.CreateDeviceTokenRequest;
import com.memozy.memozy_back.domain.alert.repository.DeviceTokenRepository;
import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.global.exception.ErrorCode;
import com.memozy.memozy_back.global.exception.GlobalException;
import com.memozy.memozy_back.global.util.EntityLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final EntityLoader entityLoader;

    public void registerDeviceToken(final Long userId, CreateDeviceTokenRequest request) {
        User user = entityLoader.getUser(userId);

        deviceTokenRepository.findByExpoToken(request.deviceToken()).ifPresent(existing -> {
            Long ownerId = existing.getUser().getId();
            if (!ownerId.equals(userId)) {
                throw new GlobalException(ErrorCode.DEVICE_TOKEN_OWNED_BY_ANOTHER_USER);
            }
        });

        DeviceToken deviceToken = DeviceToken.create(
                user,
                request.deviceToken(),
                request.platform()
        );

        deviceTokenRepository.save(deviceToken);
    }
}