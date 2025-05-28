package com.memozy.memozy_back.domain.user.dto.response;

import com.memozy.memozy_back.domain.user.domain.User;

public record UpdateUserResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        String email,
        String phoneNumber
) {

    public static UpdateUserResponse of(User user, String presignedUrl) {
        return new UpdateUserResponse(
                user.getId(),
                user.getNickname(),
                presignedUrl,
                user.getEmail(),
                user.getPhoneNumber()
        );
    }
}
