package com.memozy.memozy_back.domain.friend.repository;

import com.memozy.memozy_back.domain.friend.domain.Friendship;
import com.memozy.memozy_back.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // 이미 보낸 친구 요청 확인
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END " +
            "FROM Friendship f " +
            "WHERE (f.sender.id = :senderId AND f.receiver.id = :receiverId) " +
            "   OR (f.sender.id = :receiverId AND f.receiver.id = :senderId)")
    boolean existsFriendship(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    // 특정 요청 가져오기
    @Query("SELECT f FROM Friendship f " +
            "WHERE (f.sender.id = :senderId AND f.receiver.id = :receiverId) " +
            "   OR (f.sender.id = :receiverId AND f.receiver.id = :senderId)")
    Optional<Friendship> findRequest(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    // 친구 요청 수락 시 양방향 삭제
    @Modifying
    @Query("DELETE FROM Friendship f "
            + "WHERE (f.sender.id = :userId AND f.receiver.id = :friendId) "
            + "OR (f.sender.id = :friendId AND f.receiver.id = :userId)")
    void deleteFriendship(@Param("userId") Long userId, @Param("friendId") Long friendId);

    // 친구 목록
    // 내가 보낸 친구 요청 중 수락된 것
    @Query("SELECT f.receiver FROM Friendship f WHERE f.sender.id = :userId AND f.status = 'ACCEPTED'")
    List<User> findAcceptedFriendsSentBy(@Param("userId") Long userId);

    // 내가 받은 친구 요청 중 수락한 것
    @Query("SELECT f.sender FROM Friendship f WHERE f.receiver.id = :userId AND f.status = 'ACCEPTED'")
    List<User> findAcceptedFriendsReceivedBy(@Param("userId") Long userId);

    // 보낸 친구 요청 목록 (PENDING)
    @Query("""
        SELECT f.receiver FROM Friendship f
        WHERE f.sender.id = :userId AND f.status = com.memozy.memozy_back.domain.friend.constant.FriendshipStatus.REQUESTED
    """)
    List<User> findSentRequests(Long userId);

    // 받은 친구 요청 목록 (PENDING)
    @Query("""
        SELECT f.sender FROM Friendship f
        WHERE f.receiver.id = :userId AND f.status = com.memozy.memozy_back.domain.friend.constant.FriendshipStatus.REQUESTED
    """)
    List<User> findReceivedRequests(Long userId);
}