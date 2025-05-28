package com.memozy.memozy_back.domain.user.dto.response;

import com.memozy.memozy_back.domain.user.domain.User;

public record GetUserProfileResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        String email,
        String phoneNumber
) {

    public static GetUserProfileResponse of(User user, String presignedUrl) {
        return new GetUserProfileResponse(
                user.getId(),
                user.getNickname(),
                presignedUrl,
                user.getEmail(),
                user.getPhoneNumber()
        );
    }
}

