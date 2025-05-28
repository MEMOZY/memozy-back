package com.memozy.memozy_back.domain.user.dto;

import com.memozy.memozy_back.domain.user.domain.User;

public record UserDto(
        Long userId,
        String nickname,
        String profileImageUrl,
        String email,
        String phoneNumber,
        String friendCode
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getFriendCode()
        );
    }
}