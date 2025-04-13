package com.memozy.memozy_back.domain.friend.controller;

import com.memozy.memozy_back.domain.friend.dto.response.GetFriendInfoListResponse;
import com.memozy.memozy_back.domain.friend.service.FriendshipService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "친구 관리 API", description = "친구 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/friends")
public class FriendshipController {

    private final FriendshipService friendshipService;

    // 친구 요청
    @PostMapping("/{userId}/request/{targetUserId}")
    public ResponseEntity<Void> sendFriendRequest(
            @PathVariable Long userId,
            @PathVariable Long targetUserId
    ) {
        friendshipService.sendFriendRequest(userId, targetUserId);
        return ResponseEntity.ok().build();
    }

    // 친구 수락
    @PostMapping("/{userId}/accept/{targetUserId}")
    public ResponseEntity<Void> acceptFriendRequest(
            @PathVariable Long userId,
            @PathVariable Long targetUserId
    ) {
        friendshipService.acceptFriendRequest(userId, targetUserId);
        return ResponseEntity.ok().build();
    }

    // 친구 거절
    @PostMapping("/{userId}/reject/{targetUserId}")
    public ResponseEntity<Void> rejectFriendRequest(
            @PathVariable Long userId,
            @PathVariable Long targetUserId
    ) {
        friendshipService.rejectFriendRequest(userId, targetUserId);
        return ResponseEntity.ok().build();
    }

    // 친구 삭제
    @DeleteMapping("/{userId}/remove/{targetUserId}")
    public ResponseEntity<Void> removeFriend(
            @PathVariable Long userId,
            @PathVariable Long targetUserId
    ) {
        friendshipService.deleteFriend(userId, targetUserId);
        return ResponseEntity.noContent().build();
    }

    // 친구 목록 조회
    @GetMapping("/{userId}/list")
    public ResponseEntity<GetFriendInfoListResponse> getFriends(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(friendshipService.getFriends(userId));
    }

    // 친구 요청 보낸 목록 조회
    @GetMapping("/{userId}/sent-requests")
    public ResponseEntity<GetFriendInfoListResponse> getSentRequests(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(friendshipService.getSentRequests(userId));
    }

    // 친구 요청 받은 목록 조회
    @GetMapping("/{userId}/received-requests")
    public ResponseEntity<GetFriendInfoListResponse> getReceivedRequests(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(friendshipService.getReceivedRequests(userId));
    }
}