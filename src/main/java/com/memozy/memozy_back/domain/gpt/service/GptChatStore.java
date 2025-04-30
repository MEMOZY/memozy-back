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

    public void initConversation(Long memoryItemId) {
        chatHistories.put(memoryItemId, new ArrayList<>());
    }

    public void addUserMessage(Long memoryItemId, String content) {
        chatHistories.get(memoryItemId).add(ChatMessage.user(content));
    }

    public void addAssistantMessage(Long memoryItemId, String content) {
        chatHistories.get(memoryItemId).add(ChatMessage.assistant(content));
    }

    public List<ChatMessage> getChat(Long memoryItemId) {
        return chatHistories.getOrDefault(memoryItemId, List.of());
    }

    public void removeChat(Long memoryItemId) {
        chatHistories.remove(memoryItemId);
    }

    public int getUserMessageCount(Long memoryItemId) {
        return chatHistories.get(memoryItemId).stream()
                .filter(chat -> chat.role().equals("user"))
                .toList()
                .size();
    }
}