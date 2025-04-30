package com.memozy.memozy_back.domain.gpt.service;

import com.memozy.memozy_back.domain.gpt.constant.PromptText;
import com.memozy.memozy_back.domain.gpt.dto.response.OpenAiChatResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class GptClientImpl implements GptClient {

    private final WebClient webClient = WebClient.create("https://api.openai.com/v1");

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    @Override
    public String initiateChatWithImage(String base64Image) {
        List<Map<String, Object>> messages = List.of(
                Map.of("role", "system", "content", PromptText.FIRST_COMMENT_PROMPT.getText()),
                Map.of("role", "user", "content", List.of(
                        Map.of("type", "image_url", "image_url", Map.of(
                                "url", "data:image/jpeg;base64," + base64Image,
                                "detail", "low"
                        ))
                ))
        );
        return callChatApi(messages);
    }

    @Override
    public String sendMessage(List<String> messagesSoFar) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", PromptText.TEXT_PROMPT.getText()));

        for (int i = 0; i < messagesSoFar.size(); i++) {
            messages.add(Map.of(
                    "role", (i % 2 == 0) ? "user" : "assistant",
                    "content", messagesSoFar.get(i)
            ));
        }

        return callChatApi(messages);
    }

    @Override
    public String generateStoryFromChatAndImage(List<String> messagesSoFar, String base64Image) {
        List<Map<String, Object>> messages = new ArrayList<>();

        messages.add(Map.of("role", "user", "content", PromptText.IMG_PROMPT.getText()));

        for (int i = 0; i < messagesSoFar.size(); i++) {
            messages.add(Map.of(
                    "role", (i % 2 == 0) ? "user" : "assistant",
                    "content", messagesSoFar.get(i)
            ));
        }

        messages.add(Map.of("role", "user", "content", List.of(
                Map.of("type", "text", "text", "위의 대화 내용을 참고해서 이 이미지에 대한 일기를 작성해줘."),
                Map.of("type", "image_url", "image_url", Map.of(
                        "url", "data:image/jpeg;base64," + base64Image,
                        "detail", "high"
                ))
        )));

        return callChatApi(messages);
    }

    private String callChatApi(List<Map<String, Object>> messages) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", messages
        );

        OpenAiChatResponse response = webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(OpenAiChatResponse.class)
                .block();

        return response.choices().getFirst().message().content();
    }
}