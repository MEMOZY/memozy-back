package com.memozy.memozy_back.domain.gpt.service;

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
public class RedisTemporaryChatStore {


    private final RedisTemplate<String, List<ChatMessage>> chatRedisTemplate;
    private static final Duration TTL = Duration.ofMinutes(1440);

    public void initChat(String memoryItemTempId) {
        chatRedisTemplate.opsForValue().set(memoryItemTempId, new ArrayList<>(), TTL);
    }

    public void addUserMessage(String memoryItemTempId, String content) {
        List<ChatMessage> history = getChat(memoryItemTempId);
        history.add(ChatMessage.user(content));
        chatRedisTemplate.opsForValue().set(memoryItemTempId, history, TTL);
    }

    public void addAssistantMessage(String memoryItemTempId, String content) {
        List<ChatMessage> history = getChat(memoryItemTempId);
        history.add(ChatMessage.assistant(content));
        chatRedisTemplate.opsForValue().set(memoryItemTempId, history, TTL);
    }

    public List<ChatMessage> getChat(String memoryItemTempId) {
        List<ChatMessage> chat = chatRedisTemplate.opsForValue().get(memoryItemTempId);
        return chat != null ? chat : new ArrayList<>();
    }

    public void removeChat(String memoryItemTempId) {
        chatRedisTemplate.delete(memoryItemTempId);
    }

    public int getUserMessageCount(String memoryItemTempId) {
        return (int) getChat(memoryItemTempId).stream()
                .filter(m -> m.role().equals("user"))
                .count();
    }
}