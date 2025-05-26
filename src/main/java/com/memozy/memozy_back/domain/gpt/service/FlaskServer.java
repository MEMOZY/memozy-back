package com.memozy.memozy_back.domain.gpt.service;

import java.util.List;

public interface FlaskServer {
    String initiateChatWithImageUrl(String sessionId, String presignedImageUrl);

    String sendMessage(String sessionId, String presignedUrl, String userMessage, List<String> messages);

    String generateDiaryFromChatAndImageUrl(String sessionId, List<String> messages, String presignedUrl);
}
