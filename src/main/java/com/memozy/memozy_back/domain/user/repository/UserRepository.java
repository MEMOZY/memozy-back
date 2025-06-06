package com.memozy.memozy_back.domain.user.repository;

import com.memozy.memozy_back.domain.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByFriendCode(String friendCode);

    Optional<User> findByEmail(String email);
}
