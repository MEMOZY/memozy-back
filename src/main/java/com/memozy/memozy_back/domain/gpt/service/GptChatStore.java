package com.memozy.memozy_back.domain.gpt.service;

import com.memozy.memozy_back.domain.gpt.dto.ChatMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class GptChatStore {
    private final Map<Long, List<ChatMessage>> chatHistories = new ConcurrentHashMap<>();

    public void initChat(Long memoryItemTempId) {
        System.out.println("memoryItemTempId = " + memoryItemTempId);
        chatHistories.put(memoryItemTempId, new ArrayList<>());
        System.out.println("chatHistories.get(memoryItemTempId) = " + chatHistories.get(memoryItemTempId));
    }

    public void addUserMessage(Long memoryItemTempId, String content) {
        chatHistories.get(memoryItemTempId).add(ChatMessage.user(content));
    }

    public void addAssistantMessage(Long memoryItemTempId, String content) {
        chatHistories.get(memoryItemTempId).add(ChatMessage.assistant(content));
    }

    public List<ChatMessage> getChat(Long memoryItemTempId) {
        return chatHistories.getOrDefault(memoryItemTempId, List.of());
    }

    public void removeChat(Long memoryItemTempId) {
        chatHistories.remove(memoryItemTempId);
    }

    public int getUserMessageCount(Long memoryItemTempId) {
        return chatHistories.get(memoryItemTempId).stream()
                .filter(chat -> chat.role().equals("user"))
                .toList()
                .size();
    }
}