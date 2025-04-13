package com.memozy.memozy_back.domain.friend.service.impl;

import com.memozy.memozy_back.domain.friend.domain.Friendship;
import com.memozy.memozy_back.domain.friend.dto.FriendInfoDto;
import com.memozy.memozy_back.domain.friend.dto.response.GetFriendInfoListResponse;
import com.memozy.memozy_back.domain.friend.repository.FriendshipRepository;
import com.memozy.memozy_back.domain.friend.service.FriendshipService;
import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.domain.user.repository.UserRepository;
import com.memozy.memozy_back.global.exception.BusinessException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void sendFriendRequest(Long senderId, Long receiverId) {
        User sender = getUserById(senderId);
        User receiver = getUserById(receiverId);

        if (friendshipRepository.existsBySenderAndReceiver(sender, receiver)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE_EXCEPTION);
        }

        Friendship friendship = Friendship.create(sender, receiver);
        friendshipRepository.save(friendship);
    }

    @Override
    @Transactional
    public void acceptFriendRequest(Long senderId, Long receiverId) {
        Friendship friendship = friendshipRepository.findBySenderIdAndReceiverId(senderId, receiverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION));
        friendship.accept();
    }

    @Override
    @Transactional
    public void rejectFriendRequest(Long senderId, Long receiverId) {
        Friendship friendship = friendshipRepository.findBySenderIdAndReceiverId(senderId, receiverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION));
        friendshipRepository.delete(friendship);
    }

    @Override
    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        friendshipRepository.deleteFriendship(userId, friendId);
    }

    @Override
    @Transactional(readOnly = true)
    public GetFriendInfoListResponse getFriends(Long userId) {
        List<FriendInfoDto> friends = friendshipRepository.findAcceptedFriends(userId).stream()
                .map(FriendInfoDto::from)
                .toList();
        return GetFriendInfoListResponse.from(friends);
    }

    @Override
    @Transactional(readOnly = true)
    public GetFriendInfoListResponse getSentRequests(Long userId) {
        List<FriendInfoDto> sentRequests = friendshipRepository.findSentRequests(userId).stream()
                .map(FriendInfoDto::from)
                .toList();
        return GetFriendInfoListResponse.from(sentRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public GetFriendInfoListResponse getReceivedRequests(Long userId) {
        List<FriendInfoDto> receivedRequests = friendshipRepository.findReceivedRequests(userId).stream()
                .map(FriendInfoDto::from)
                .toList();
        return GetFriendInfoListResponse.from(receivedRequests);
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER_EXCEPTION));
    }
}