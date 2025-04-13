package com.memozy.memozy_back.domain.friend.dto.response;

import com.memozy.memozy_back.domain.friend.dto.FriendInfoDto;
import java.util.List;

public record GetFriendInfoListResponse(
        List<FriendInfoDto> friends
) {
    public static GetFriendInfoListResponse from(List<FriendInfoDto> friendInfoList) {
        return new GetFriendInfoListResponse(friendInfoList);
    }
}