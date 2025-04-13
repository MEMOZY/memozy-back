package com.memozy.memozy_back.domain.friend.service;

import com.memozy.memozy_back.domain.friend.dto.response.GetFriendInfoListResponse;

public interface FriendshipService {

    void sendFriendRequest(Long senderId, Long receiverId);
    void acceptFriendRequest(Long senderId, Long receiverId);
    void rejectFriendRequest(Long senderId, Long receiverId);
    void deleteFriend(Long userId, Long friendId);
    GetFriendInfoListResponse getFriends(Long userId);
    GetFriendInfoListResponse getSentRequests(Long userId);
    GetFriendInfoListResponse getReceivedRequests(Long userId);


}