package com.memozy.memozy_back.domain.gpt.service;

import com.memozy.memozy_back.domain.gpt.dto.ChatMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class GptChatStore {
    private final Map<String, List<ChatMessage>> chatHistories = new ConcurrentHashMap<>();

    public void initChat(String memoryItemTempId) {
        chatHistories.put(memoryItemTempId, new ArrayList<>());
    }

    public void addUserMessage(String memoryItemTempId, String content) {
        chatHistories.get(memoryItemTempId).add(ChatMessage.user(content));
    }

    public void addAssistantMessage(String memoryItemTempId, String content) {
        chatHistories.get(memoryItemTempId).add(ChatMessage.assistant(content));
    }

    public List<ChatMessage> getChat(String memoryItemTempId) {
        return chatHistories.getOrDefault(memoryItemTempId, List.of());
    }

    public void removeChat(String memoryItemTempId) {
        chatHistories.remove(memoryItemTempId);
    }

    public int getUserMessageCount(String memoryItemTempId) {
        return chatHistories.get(memoryItemTempId).stream()
                .filter(chat -> chat.role().equals("user"))
                .toList()
                .size();
    }
}