package com.memozy.memozy_back.domain.alert.service;

import com.memozy.memozy_back.domain.alert.constant.PushAlertMsg;
import com.memozy.memozy_back.domain.alert.repository.DeviceTokenRepository;
import com.memozy.memozy_back.domain.memory.dto.MemorySharedEvent;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpoPushSender {

    private final DeviceTokenRepository deviceTokenRepository;

    private final WebClient expo = WebClient.builder()
            .baseUrl("https://exp.host")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();

    private static final int BATCH_SIZE = 100;

    public void sendSharedInfo(MemorySharedEvent memorySharedEvent) {
        List<String> tokens =
                deviceTokenRepository.findValidExpoTokensByUserIds(memorySharedEvent.recipientIds());
        if (tokens.isEmpty()) return;

        String title = PushAlertMsg.SHARED_MEMORY_TITLE.msg();
        String body = PushAlertMsg.SHARED_MEMORY_BODY.format(memorySharedEvent.actorUserNickName());
        Map<String, Object> data = Map.of(
                "type", "MEMORY_SHARED",
                "memoryId", memorySharedEvent.memoryId(),
                "by", memorySharedEvent.actorUserId()
        );

        for (int i = 0; i < tokens.size(); i += BATCH_SIZE) {
            List<String> slice = tokens.subList(i, Math.min(i + BATCH_SIZE, tokens.size()));
            List<Map<String, Object>> msgs = buildMessages(slice, title, body, data);
            sendAlertInfos(slice, msgs);
        }
    }

    private List<Map<String, Object>> buildMessages(List<String> tokens,
            String title,
            String body,
            Map<String, Object> data) {
        return tokens.stream()
                .map(t -> Map.of(
                        "to", t,
                        "title", title,
                        "body", body,
                        "data", data,
                        "sound", "default",
                        "channelId", "default",
                        "priority", "high"
                ))
                .toList();
    }

    private void sendAlertInfos(List<String> tokens, List<Map<String, Object>> msgs) {
        try {
            Map<String, Object> response = expo.post()
                    .uri("/--/api/v2/push/send")
                    .bodyValue(msgs)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response != null) {
                handleResponse(tokens, response);
            }
        } catch (WebClientResponseException ex) {
            log.error("Expo push HTTP error status={} body={}", ex.getRawStatusCode(),
                    ex.getResponseBodyAsString(), ex);
        } catch (WebClientRequestException ex) {
            log.error("Expo push network error: {}", ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Expo push unknown error", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleResponse(List<String> tokens, Map<String, Object> response) {
        Object dataObj = response.get("data");
        if (!(dataObj instanceof List<?> items)) {
            log.warn("Unexpected expo response shape: {}", response);
            return;
        }

        int limit = Math.min(items.size(), tokens.size());
        for (int i = 0; i < limit; i++) {
            Object item = items.get(i);
            if (!(item instanceof Map<?, ?> itemMap)) continue;

            String token = tokens.get(i);
            String status = String.valueOf(itemMap.get("status"));
            if ("ok".equalsIgnoreCase(status)) continue;

            String message = String.valueOf(itemMap.get("message"));
            Map<String, Object> details =
                    (itemMap.get("details") instanceof Map<?, ?> d) ? (Map<String, Object>) d : null;

            String errorCode = extractErrorCode(itemMap, details);
            log.warn("Expo push error token={} status={} message={} error={}",
                    token, status, message, errorCode);

            if ("DeviceNotRegistered".equals(errorCode) || "DeviceNotRegistered".equals(message)) {
                invalidateToken(token);
            }
        }
    }

    private String extractErrorCode(Map<?, ?> itemMap, Map<String, Object> details) {
        if (details != null && details.get("error") != null) {
            return details.get("error").toString();
        }
        Object topErr = itemMap.get("error");
        return topErr != null ? topErr.toString() : "UNKNOWN";
    }

    private void invalidateToken(String token) {
        try {
            int updated = deviceTokenRepository.invalidateByExpoToken(token);
            if (updated > 0) {
                log.info("Invalidated expo token={}", token);
            }
        } catch (Exception ex) {
            log.error("Failed to invalidate token={}", token, ex);
        }
    }
}