package com.memozy.memozy_back.domain.gpt.service;

import java.util.List;
import java.util.Map;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface FlaskServer {
    void initiateChatWithImageUrl(String sessionId, String presignedImageUrl, String memoryItemTempId, SseEmitter emitter);

    String sendMessage(String sessionId, String presignedUrl, String userMessage, Map<String, List<String>> history);

    String generateDiaryFromChatAndImageUrl(String sessionId, Map<String, List<String>> history, String presignedUrl);

    List<Map<String, String>> generateFinalDiaries(String sessionId, List<Map<String, String>> diaryList);
}
