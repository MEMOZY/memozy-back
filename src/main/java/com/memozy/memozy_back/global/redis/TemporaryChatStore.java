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

    private final RedisTemplate<String, Map<String, Map<String, List<String>>>> chatRedisTemplate;
    private final RedisTemplate<String, String> activeMemoryItemRedisTemplate;
    private static final Duration TTL = Duration.ofMinutes(1440);

    public void initSession(String sessionId) {
        chatRedisTemplate.opsForValue().set(sessionId, new ConcurrentHashMap<>(), TTL);
    }

    public void initChat(String sessionId, String memoryItemId) {
        Map<String, Map<String, List<String>>> sessionChats = getChatsAll(sessionId);
        sessionChats.put(memoryItemId, initRoleMap());
        chatRedisTemplate.opsForValue().set(sessionId, sessionChats, TTL);

        // 현재 활성 memoryItemId 업데이트
        setActiveMemoryItemId(sessionId, memoryItemId);
    }

    public void setActiveMemoryItemId(String sessionId, String memoryItemId) {
        activeMemoryItemRedisTemplate.opsForValue().set(sessionId + ":active", memoryItemId, TTL);
    }

    public String getActiveMemoryItemId(String sessionId) {
        return activeMemoryItemRedisTemplate.opsForValue().get(sessionId + ":active");
    }

    public void addUserMessage(String sessionId, String memoryItemId, String content) {
        Map<String, Map<String, List<String>>> sessionChats = getChatsAll(sessionId);
        sessionChats.computeIfAbsent(memoryItemId, k -> initRoleMap()).get("user").add(content);
        chatRedisTemplate.opsForValue().set(sessionId, sessionChats, TTL);
    }

    public void addAssistantMessage(String sessionId, String memoryItemId, String content) {
        Map<String, Map<String, List<String>>> sessionChats = getChatsAll(sessionId);
        sessionChats.computeIfAbsent(memoryItemId, k -> initRoleMap()).get("assistant").add(content);
        chatRedisTemplate.opsForValue().set(sessionId, sessionChats, TTL);
    }

    public Map<String, List<String>> getChatHistorySplitByRole(String sessionId, String memoryItemId) {
        Map<String, Map<String, List<String>>> sessionChats = getChatsAll(sessionId);
        return sessionChats.getOrDefault(memoryItemId, initRoleMap());
    }

    public int getTurnCount(String sessionId, String memoryItemId) {
        Map<String, Map<String, List<String>>> sessionChats = getChatsAll(sessionId);
        List<String> userMessages = sessionChats.getOrDefault(memoryItemId, initRoleMap()).get("user");
        List<String> assistantMessages = sessionChats.getOrDefault(memoryItemId, initRoleMap()).get("assistant");
        return Math.min(userMessages.size(), assistantMessages.size());
    }

    public void removeSession(String sessionId) {
        chatRedisTemplate.delete(sessionId);
        activeMemoryItemRedisTemplate.delete(sessionId + ":active");
    }


    private Map<String, Map<String, List<String>>> getChatsAll(String sessionId) {
        Map<String, Map<String, List<String>>> sessionChats = chatRedisTemplate.opsForValue().get(sessionId);
        return sessionChats != null ? sessionChats : new ConcurrentHashMap<>();
    }

    private Map<String, List<String>> initRoleMap() {
        Map<String, List<String>> roleMap = new ConcurrentHashMap<>();
        roleMap.put("user", new ArrayList<>());
        roleMap.put("assistant", new ArrayList<>());
        return roleMap;
    }
}
