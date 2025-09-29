package com.memozy.memozy_back.domain.alert.repository;

import com.memozy.memozy_back.domain.alert.domain.DeviceToken;
import com.memozy.memozy_back.domain.alert.domain.Platform;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByExpoToken(String expoToken);

    @Modifying
    @Query("delete from DeviceToken t where t.user.id = :userId and t.platform = :platform")
    void deleteByUserIdAndPlatform(@Param("userId") Long userId, @Param("platform") Platform platform);

    @Query("select t.expoToken from DeviceToken t where t.user.id in :userIds and t.isValid = true")
    List<String> findValidExpoTokensByUserIds(@Param("userIds") List<Long> userIds);

    @Modifying
    @Query("update DeviceToken t set t.isValid=false where t.expoToken=:token")
    int invalidateByExpoToken(@Param("token") String token);
}
