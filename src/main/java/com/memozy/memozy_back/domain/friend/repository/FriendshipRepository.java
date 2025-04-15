package com.memozy.memozy_back.domain.friend.repository;

import com.memozy.memozy_back.domain.friend.domain.Friendship;
import com.memozy.memozy_back.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // 이미 보낸 친구 요청 확인
    boolean existsBySenderAndReceiver(User sender, User receiver);

    // 특정 요청 가져오기
    Optional<Friendship> findBySenderIdAndReceiverId(Long senderId, Long receiverId);

    // 친구 요청 수락 시 양방향 삭제
    @Query("DELETE FROM Friendship f WHERE (f.sender.id = :userId AND f.receiver.id = :friendId) OR (f.sender.id = :friendId AND f.receiver.id = :userId)")
    void deleteFriendship(Long userId, Long friendId);

    // 수락된 친구 목록
    @Query("""
        SELECT CASE
          WHEN f.sender.id = :userId THEN f.receiver
          ELSE f.sender
        END
        FROM Friendship f
        WHERE (f.sender.id = :userId OR f.receiver.id = :userId)
        AND f.status = com.memozy.memozy_back.domain.friend.domain.FriendshipStatus.ACCEPTED
    """)
    List<User> findAcceptedFriends(Long userId);

    // 보낸 친구 요청 목록 (PENDING)
    @Query("""
        SELECT f.receiver FROM Friendship f
        WHERE f.sender.id = :userId AND f.status = com.memozy.memozy_back.domain.friend.domain.FriendshipStatus.REQUESTED
    """)
    List<User> findSentRequests(Long userId);

    // 받은 친구 요청 목록 (PENDING)
    @Query("""
        SELECT f.sender FROM Friendship f
        WHERE f.receiver.id = :userId AND f.status = com.memozy.memozy_back.domain.friend.domain.FriendshipStatus.REQUESTED
    """)
    List<User> findReceivedRequests(Long userId);
}