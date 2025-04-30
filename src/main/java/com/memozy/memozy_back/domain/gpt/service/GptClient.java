package com.memozy.memozy_back.domain.gpt.service;

import java.util.List;

public interface GptClient {

    String initiateChatWithImage(String base64Image);

    String sendMessage(List<String> messages);

    String generateStoryFromChatAndImage(List<String> messages, String base64Image);
}