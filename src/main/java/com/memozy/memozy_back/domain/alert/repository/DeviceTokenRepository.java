package com.memozy.memozy_back.domain.alert.repository;

import com.memozy.memozy_back.domain.alert.domain.DeviceToken;
import com.memozy.memozy_back.domain.alert.domain.Platform;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByExpoToken(String expoToken);

    @Modifying
    @Query("delete from DeviceToken t where t.user.id = :userId and t.platform = :platform")
    int deleteByUserIdAndPlatform(@Param("userId") Long userId, @Param("platform") Platform platform);
}
