package com.memozy.memozy_back.domain.friend.dto;

import com.memozy.memozy_back.domain.user.domain.User;

public record FriendInfoDto(
        Long userId,
        String nickname,
        String profileImageUrl
) {
    public static FriendInfoDto from(User user) {
        return new FriendInfoDto(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl()
        );
    }
}
