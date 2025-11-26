package com.memozy.memozy_back.domain.user.repository;

import com.memozy.memozy_back.domain.user.constant.SocialPlatform;
import com.memozy.memozy_back.domain.user.domain.SocialUserInfo;
import com.memozy.memozy_back.domain.user.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SocialUserInfoRepository extends JpaRepository<SocialUserInfo, Long> {

    @Query(value = "SELECT * FROM social_user_infos WHERE social_code = :code LIMIT 1", nativeQuery = true)
    Optional<SocialUserInfo> findAnyBySocialCode(@Param("code") String code);

    void deleteByUser(User user);

    boolean existsByUserAndSocialType(User existingUser, SocialPlatform socialPlatform);

    Optional<SocialUserInfo> findByUserId(Long userId);

    @Modifying
    @Query(value = "UPDATE social_user_infos SET is_deleted = false, user_id = :userId "
            + "WHERE social_user_info_id = :id", nativeQuery = true)
    int reactivateById(@Param("id") Long id, @Param("userId") Long userId);
}
