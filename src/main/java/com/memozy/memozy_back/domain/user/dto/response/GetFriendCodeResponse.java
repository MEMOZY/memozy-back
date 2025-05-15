package com.memozy.memozy_back.domain.user.dto.response;

public record GetFriendCodeResponse(
        String friendCode
){

    public static GetFriendCodeResponse from(String friendCode) {
        return new GetFriendCodeResponse(
                friendCode
        );
    }
}