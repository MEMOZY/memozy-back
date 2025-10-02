package com.memozy.memozy_back.domain.chat.service;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Deprecated
public interface GptClient {

    String initiateChatWithImage(String base64Image);

    String sendMessage(List<String> messages);

    void streamChatApi(List<Map<String, Object>> messages, Consumer<String> onDelta, Runnable onComplete, Consumer<Throwable> onError);

    String generateDiaryFromChatAndImage(List<String> messages, String base64Image);
}