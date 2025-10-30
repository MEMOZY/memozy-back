package com.memozy.memozy_back.domain.user.dto.response;

import com.memozy.memozy_back.domain.user.domain.User;

public record GetUserInfoResponse(
        Long userId,
        String profileImageUrl
) {

    public static GetUserInfoResponse from(User user, String presignedUrl) {
        return new GetUserInfoResponse(
                user.getId(),
                presignedUrl
        );
    }
}
