package com.memozy.memozy_back.domain.gpt.service;

import com.memozy.memozy_back.global.exception.BusinessException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
@RequiredArgsConstructor
public class FlaskServerImpl implements FlaskServer {

    private final WebClient webClient = WebClient.create("http://memozy-ai:5000");

    @Override
    public String initiateChatWithImageUrl(String sessionId, String presignedImageUrl) {
        Map<String, Object> requestBody = Map.of(
                "session_id", sessionId,
                "img_url", presignedImageUrl,
                "history", Map.of("user", List.of(), "assistant", List.of())
        );

        log.info("Request to /image: {}", requestBody);

        Map<String, Object> response = webClient.post()
                .uri("/image")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        log.info("Response from /image: {}", response);

        return Optional.ofNullable(response)
                .map(r -> (String) r.get("message"))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_RESPONSE_FLASK_SERVER));
    }

    @Override
    public String sendMessage(String sessionId, String presignedUrl, String userMessage, Map<String, List<String>> history) {
        Map<String, Object> requestBody = Map.of(
                "session_id", sessionId,
                "img_url", presignedUrl,
                "history", history,
                "message", userMessage
        );

        log.info("Request to /message: {}", requestBody);

        Map<String, Object> response = webClient.post()
                .uri("/message")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        log.info("Response from /message: {}", response);

        return Optional.ofNullable(response)
                .map(r -> (String) r.get("message"))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_RESPONSE_FLASK_SERVER));
    }

    @Override
    public String generateDiaryFromChatAndImageUrl(String sessionId, Map<String, List<String>> history, String presignedUrl) {
        Map<String, Object> requestBody = Map.of(
                "session_id", sessionId,
                "img_url", presignedUrl,
                "history", history
        );

        log.info("Request to /diary: {}", requestBody);

        Map<String, Object> response = webClient.post()
                .uri("/diary")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        log.info("Response from /diary: {}", response);

        return Optional.ofNullable(response)
                .map(r -> (String) r.get("diary"))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_RESPONSE_FLASK_SERVER));
    }
}