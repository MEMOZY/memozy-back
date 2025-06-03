package com.memozy.memozy_back.domain.gpt.service;

import com.memozy.memozy_back.domain.gpt.dto.EmitterPayloadDto;
import com.memozy.memozy_back.global.exception.BusinessException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import com.memozy.memozy_back.global.redis.TemporaryChatStore;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@Slf4j
@RequiredArgsConstructor
public class FlaskServerImpl implements FlaskServer {

    private final WebClient webClient = WebClient.create("http://memozy-ai:5000");
    private final TemporaryChatStore temporaryChatStore;

    @Override
    public void initiateChatWithImageUrl(String sessionId, String presignedImageUrl,
            String memoryItemTempId, SseEmitter emitter, Runnable onCompleteCallback) {
        StringBuilder completeReply = new StringBuilder();

        webClient.post()
                .uri("/image")
                .bodyValue(Map.of(
                        "session_id", sessionId,
                        "img_url", presignedImageUrl,
                        "history", Map.of("user", List.of(), "assistant", List.of())
                ))
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .filter(chunk -> chunk != null && chunk.data() != null)
                .doOnSubscribe(sub -> log.info("✅ SPRING SUBSCRIBED to /image stream"))
                .doOnNext(chunk -> {
                    String data = chunk.data();
                    log.info("➡ doOnNext: chunk.data={}", data);

                    if (data.contains("[DONE]")) {
                        log.info("✅ Detected [DONE], done 이벤트 전송 시도");
                        try {
                            sendEmitterPayload(emitter, "done", memoryItemTempId, "대화 종료", presignedImageUrl);
                        } catch (Exception e) {
                            log.warn("Failed to send DONE event", e);
                        }
                        return;
                    }

                    completeReply.append(data);
                    try {
                        sendEmitterPayload(emitter, "reply", memoryItemTempId, data, presignedImageUrl);
                    } catch (IllegalStateException ex) {
                        log.warn("❌ SSEEmitter already completed, skipping send: {}", ex.getMessage());
                    } catch (IOException e) {
                        log.error("❌ SSE 전송 중 IOException 발생: {}", e.getMessage(), e);
                    } catch (Exception e) {
                        log.error("❌ 기타 전송 예외 발생: {}", e.getMessage(), e);
                    }
                })
                .doOnError(e -> {
                    log.error("❌ SPRING ERROR: {}", e.getMessage(), e);
                    emitter.completeWithError(e);
                })
                .doOnComplete(() -> {
                    log.info("✅ SPRING STREAM COMPLETE");
                    String finalMessage = completeReply.toString();

                    temporaryChatStore.addAssistantMessage(sessionId, memoryItemTempId, finalMessage);
                    log.info("✅ SPRING SENT FINAL reply");

                    if (onCompleteCallback != null) {
                        onCompleteCallback.run();
                    }

                    emitter.complete();
                })
                .subscribe();
    }

    @Override
    public void sendMessage(String sessionId, String presignedUrl, String userMessage,
            Map<String, List<String>> history, String memoryItemTempId, SseEmitter emitter, Runnable onCompleteCallback) {
        StringBuilder completeReply = new StringBuilder();

        log.info("✅ SPRING PREPARED body: sessionId={}, presignedUrl={}, history={}, userMessage={}",
                sessionId, presignedUrl, history, userMessage);

        webClient.post()
                .uri("/message")
                .bodyValue(Map.of(
                        "session_id", sessionId,
                        "img_url", presignedUrl,
                        "history", history,
                        "message", userMessage
                ))
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .filter(chunk -> chunk != null && chunk.data() != null)
                .doOnSubscribe(sub -> log.info("✅ SPRING SUBSCRIBED to /message stream"))
                .doOnNext(chunk -> {
                    String data = chunk.data();
                    log.info("➡ doOnNext: chunk.data={}", data);

                    if (data.contains("[DONE]")) {
                        log.info("✅ Detected [DONE], done 이벤트 전송 시도");
                        try {
                            sendEmitterPayload(emitter, "done", memoryItemTempId, "대화 종료", presignedUrl);
                        } catch (Exception e) {
                            log.warn("Failed to send DONE event", e);
                        }
                        return;
                    }

                    completeReply.append(data);
                    try {
                        sendEmitterPayload(emitter, "reply", memoryItemTempId, data, presignedUrl);
                    } catch (IllegalStateException ex) {
                        log.warn("❌ SSEEmitter already completed, skipping send: {}", ex.getMessage());
                    } catch (IOException e) {
                        log.error("❌ SSE 전송 중 IOException 발생: {}", e.getMessage(), e);
                    } catch (Exception e) {
                        log.error("❌ 기타 전송 예외 발생: {}", e.getMessage(), e);
                    }
                })
                .doOnError(e -> {
                    log.error("❌ SPRING ERROR: {}", e.getMessage(), e);
                    emitter.completeWithError(e);
                })
                .doOnComplete(() -> {
                    log.info("✅ SPRING STREAM COMPLETE");
                    String finalMessage = completeReply.toString();

                    temporaryChatStore.addAssistantMessage(sessionId, memoryItemTempId, finalMessage);
                    log.info("✅ SPRING SENT FINAL reply");

                    if (onCompleteCallback != null) {
                        onCompleteCallback.run();
                    }

                    emitter.complete();
                })
                .subscribe();
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
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_RESPONSE_FLASK_SERVER));
    }

    @Override
    public List<Map<String, String>> generateFinalDiaries(String sessionId, List<Map<String, String>> diaryList) {
        Map<String, Object> requestBody = Map.of(
                "session_id", sessionId,
                "diary", diaryList
        );

        log.info("Request to /final-diary: {}", requestBody);

        Map<String, Object> response = webClient.post()
                .uri("/final-diary")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        log.info("Response from /final-diary: {}", response);

        Object diaryObj = response != null ? response.get("diary") : null;
        if (!(diaryObj instanceof List<?> diaryRawList)) {
            throw new BusinessException(ErrorCode.NO_RESPONSE_FLASK_SERVER);
        }

        return diaryRawList.stream()
                .filter(e -> e instanceof Map)
                .map(e -> {
                    Map<?, ?> rawMap = (Map<?, ?>) e;

                    return rawMap.entrySet().stream()
                            .collect(Collectors.toMap(
                                    entry -> String.valueOf(entry.getKey()),
                                    entry -> entry.getValue() != null ? String.valueOf(entry.getValue()) : ""
                            ));
                })
                .toList();
    }

    private void sendEmitterPayload(SseEmitter emitter, String type, String tempId, String message, String presignedUrl) throws IOException {
        EmitterPayloadDto payload = new EmitterPayloadDto(tempId, type, message, presignedUrl);
        log.info("➡ sendEmitterPayload 진입: type={}, tempId={}, message={}, presignedUrl={}", type, tempId, message, presignedUrl);
        try {
            emitter.send(SseEmitter.event().name(type).data(payload));
            log.info("✅ emitter.send 성공: type={}, tempId={}", type, tempId);
        } catch (IllegalStateException ex) {
            log.warn("❌ IllegalStateException 발생: emitter 이미 완료됨. type={}, tempId={}, message={}, 예외={}", type, tempId, message, ex.getMessage(), ex);
            throw ex;
        } catch (IOException ex) {
            log.error("❌ IOException 발생: type={}, tempId={}, message={}, 예외={}", type, tempId, message, ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("❌ 기타 예외 발생: type={}, tempId={}, message={}, 예외={}", type, tempId, message, ex.getMessage(), ex);
            throw ex;
        }
    }
}
