package com.memozy.memozy_back.domain.user.repository;

import com.memozy.memozy_back.domain.user.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByFriendCode(String friendCode);

    Optional<User> findByEmail(String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
       update User u
          set u.email = :email,
              u.nickname = :nickname
        where u.id = :userId
    """)
    void withdraw(Long userId, String email, String nickname);

    @Query(value = """
        SELECT u.*
          FROM users u
          JOIN social_user_infos s ON s.user_id = u.user_id
         WHERE s.social_code = :socialCode
         LIMIT 1
    """, nativeQuery = true)
    Optional<User> findAnyBySocialCode(@Param("socialCode") String socialCode);
}
