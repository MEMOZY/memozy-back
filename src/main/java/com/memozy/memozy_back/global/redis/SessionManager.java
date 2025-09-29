package com.memozy.memozy_back.global.redis;

import com.memozy.memozy_back.global.exception.GlobalException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionManager {
    private final RedisTemplate<String, Long> redisTemplate;
    private static final Duration SESSION_TTL = Duration.ofHours(1);
    private static final String SESSION_PREFIX = "session:";

    public String createSession(Long userId) {
        String sessionId = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(SESSION_PREFIX + sessionId, userId, SESSION_TTL);
        return sessionId;
    }

    public void validateSessionOwner(Long userId, String sessionId) {
        Number storedValue = redisTemplate.opsForValue().get(SESSION_PREFIX + sessionId);
        if (storedValue == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND_TEMP_MEMORY);
        }
        Long ownerId = storedValue.longValue();

        if (!ownerId.equals(userId)) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED_EXCEPTION);
        }
    }

    public void removeSession(String sessionId) {
        redisTemplate.delete(SESSION_PREFIX + sessionId);
    }
}