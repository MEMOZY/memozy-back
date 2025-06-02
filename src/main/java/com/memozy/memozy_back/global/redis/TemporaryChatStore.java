package com.memozy.memozy_back.global.redis;

import com.memozy.memozy_back.domain.gpt.dto.ChatMessage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TemporaryChatStore {

    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration TTL = Duration.ofMinutes(1440);

    public void initSession(String sessionId) {
        // 별도로 초기화할 필요 없음
    }

    public void initChat(String sessionId, String memoryItemId) {
        setActiveMemoryItemId(sessionId, memoryItemId);
    }

    public void setActiveMemoryItemId(String sessionId, String memoryItemId) {
        redisTemplate.opsForValue().set(sessionId + ":active", memoryItemId, TTL);
    }

    public String getActiveMemoryItemId(String sessionId) {
        return redisTemplate.opsForValue().get(sessionId + ":active");
    }

    public void addUserMessage(String sessionId, String memoryItemId, String content) {
        addMessage(sessionId, memoryItemId, "user", content);
    }

    public void addAssistantMessage(String sessionId, String memoryItemId, String content) {
        addMessage(sessionId, memoryItemId, "assistant", content);
    }

    private void addMessage(String sessionId, String memoryItemId, String role, String content) {
        String key = sessionId + ":" + memoryItemId + ":" + role;
        redisTemplate.opsForList().rightPush(key, content);
        redisTemplate.expire(key, TTL);
    }

    public List<String> getMessages(String sessionId, String memoryItemId, String role) {
        String key = sessionId + ":" + memoryItemId + ":" + role;
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    public int getTurnCount(String sessionId, String memoryItemId) {
        String userKey = sessionId + ":" + memoryItemId + ":user";
        String assistantKey = sessionId + ":" + memoryItemId + ":assistant";
        Long userCount = redisTemplate.opsForList().size(userKey);
        Long assistantCount = redisTemplate.opsForList().size(assistantKey);
        return Math.toIntExact(Math.min(
                userCount != null ? userCount : 0,
                assistantCount != null ? assistantCount : 0
        ));
    }

    public void removeSession(String sessionId, List<String> memoryItemIds) {
        redisTemplate.delete(sessionId + ":active");
        for (String memoryItemId : memoryItemIds) {
            redisTemplate.delete(sessionId + ":" + memoryItemId + ":user");
            redisTemplate.delete(sessionId + ":" + memoryItemId + ":assistant");
        }
    }
}