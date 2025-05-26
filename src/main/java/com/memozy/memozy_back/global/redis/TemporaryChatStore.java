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

    private final RedisTemplate<String, Map<String, List<ChatMessage>>> chatRedisTemplate;
    private static final Duration TTL = Duration.ofMinutes(1440);

    public void initSession(String sessionId) {
        chatRedisTemplate.opsForValue().set(sessionId, new ConcurrentHashMap<>(), TTL);
    }

    public void initChat(String sessionId, String memoryItemId) {
        Map<String, List<ChatMessage>> sessionChats = getSessionChats(sessionId);
        sessionChats.put(memoryItemId, new ArrayList<>());
        chatRedisTemplate.opsForValue().set(sessionId, sessionChats, TTL);
    }

    public void addUserMessage(String sessionId, String memoryItemId, String content) {
        Map<String, List<ChatMessage>> sessionChats = getSessionChats(sessionId);
        sessionChats.computeIfAbsent(memoryItemId, k -> new ArrayList<>()).add(ChatMessage.user(content));
        chatRedisTemplate.opsForValue().set(sessionId, sessionChats, TTL);
    }

    public void addAssistantMessage(String sessionId, String memoryItemId, String content) {
        Map<String, List<ChatMessage>> sessionChats = getSessionChats(sessionId);
        sessionChats.computeIfAbsent(memoryItemId, k -> new ArrayList<>()).add(ChatMessage.assistant(content)); //
        chatRedisTemplate.opsForValue().set(sessionId, sessionChats, TTL);
    }

    public List<ChatMessage> getChatAll(String sessionId, String memoryItemId) {
        Map<String, List<ChatMessage>> sessionChats = getSessionChats(sessionId);
        return sessionChats.getOrDefault(memoryItemId, new ArrayList<>());
    }

    public Map<String, List<ChatMessage>> getAllChats(String sessionId) {
        return getSessionChats(sessionId);
    }

    public void removeChat(String sessionId, String memoryItemId) {
        Map<String, List<ChatMessage>> sessionChats = getSessionChats(sessionId);
        sessionChats.remove(memoryItemId);
        chatRedisTemplate.opsForValue().set(sessionId, sessionChats, TTL);
    }

    public int getUserMessageCount(String sessionId, String memoryItemId) {
        return (int) getChatAll(sessionId, memoryItemId).stream()
                .filter(m -> m.role().equals("user"))
                .count();
    }

    private Map<String, List<ChatMessage>> getSessionChats(String sessionId) {
        Map<String, List<ChatMessage>> sessionChats = chatRedisTemplate.opsForValue().get(sessionId);
        return sessionChats != null ? sessionChats : new ConcurrentHashMap<>();
    }
}