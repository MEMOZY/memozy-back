package com.memozy.memozy_back.domain.memory.service.impl;

import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.service.TemporaryMemoryStore;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class RedisTemporaryMemoryStoreImpl implements TemporaryMemoryStore {

    private final RedisTemplate<String, Memory> memoryRedisTemplate;
    private static final Duration TTL = Duration.ofMinutes(1440);

    @Override
    public void save(String sessionId, Memory memory) {
        memoryRedisTemplate.opsForValue().set(sessionId, memory, TTL);
    }

    @Override
    public Memory load(String sessionId) {
        return memoryRedisTemplate.opsForValue().get(sessionId);
    }

    @Override
    public void remove(String sessionId) {
        memoryRedisTemplate.delete(sessionId);
    }
}