package com.memozy.memozy_back.domain.user.repository;

import com.memozy.memozy_back.domain.user.constant.SocialPlatform;
import com.memozy.memozy_back.domain.user.domain.SocialUserInfo;
import com.memozy.memozy_back.domain.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SocialUserInfoRepository extends JpaRepository<SocialUserInfo, Long> {

    @Query("select s from SocialUserInfo s "
            + "left join fetch s.user u "
            + "where s.socialCode = :socialCode")
    Optional<SocialUserInfo> findBySocialCode(String socialCode);

    void deleteByUserId(Long userId);

    boolean existsByUserAndSocialType(User existingUser, SocialPlatform socialPlatform);
}
