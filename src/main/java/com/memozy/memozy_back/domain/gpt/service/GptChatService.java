package com.memozy.memozy_back.domain.gpt.service;

import com.memozy.memozy_back.domain.file.service.FileService;
import com.memozy.memozy_back.domain.gpt.constant.PromptText;
import com.memozy.memozy_back.domain.gpt.dto.EmitterPayloadDto;
import com.memozy.memozy_back.domain.gpt.dto.request.UserAnswerRequest;
import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import com.memozy.memozy_back.domain.memory.dto.TempMemoryDto;
import com.memozy.memozy_back.domain.memory.dto.TempMemoryItemDto;
import com.memozy.memozy_back.global.redis.TemporaryMemoryStore;
import com.memozy.memozy_back.global.exception.BusinessException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import com.memozy.memozy_back.global.redis.TemporaryChatStore;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@Slf4j
@RequiredArgsConstructor
public class GptChatService {

    private final TemporaryMemoryStore temporaryMemoryStore;
    private final FileService fileService;
    private final TemporaryChatStore temporaryChatStore;
    private final FlaskServer flaskServer;

    // ✅ 공용 ExecutorService (애플리케이션 종료 시 shutdown 필요)
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public void generateInitialPrompts(String sessionId, SseEmitter emitter) {
        AtomicBoolean isCompleted = new AtomicBoolean(false);

        executor.submit(() -> {
            try {
                handleInitialPrompt(sessionId, emitter, isCompleted);
            } catch (Exception e) {
                log.error("Initial prompt 처리 중 오류", e);
                safeCompleteWithError(emitter, e, isCompleted);
            }
        });
    }

    private void handleInitialPrompt(String sessionId, SseEmitter emitter, AtomicBoolean isCompleted) {
        Memory memory = loadMemory(sessionId);
        MemoryItem firstItem = getFirstMemoryItem(memory);

        temporaryChatStore.initSession(sessionId);
        temporaryChatStore.initChat(sessionId, firstItem.getTempId());

        String presignedUrl = getPresignedUrl(firstItem.getFileKey());

        flaskServer.initiateChatWithImageUrl(sessionId, presignedUrl, firstItem.getTempId(), wrapEmitter(emitter, isCompleted));
    }

    public void handleUserAnswer(String sessionId, UserAnswerRequest request, SseEmitter emitter) {
        AtomicBoolean isCompleted = new AtomicBoolean(false);

        executor.submit(() -> {
            try {
                handleAnswerLogic(sessionId, request, emitter, isCompleted);
            } catch (Exception e) {
                log.error("User answer 처리 중 오류", e);
                safeCompleteWithError(emitter, e, isCompleted);
            }
        });
    }

    private void handleAnswerLogic(String sessionId, UserAnswerRequest request, SseEmitter emitter, AtomicBoolean isCompleted) throws IOException {
        String memoryItemTempId = request.memoryItemTempId();
        String userMessage = request.userAnswer().trim();

        Memory memory = loadMemory(sessionId);
        MemoryItem currentItem = getMemoryItemById(memory, memoryItemTempId);

        String activeMemoryItemId = temporaryChatStore.getActiveMemoryItemId(sessionId);
        if (!memoryItemTempId.equals(activeMemoryItemId)) {
            log.warn("잘못된 memoryItemId 요청: expected={}, received={}", activeMemoryItemId, memoryItemTempId);
            sendEmitterPayload(emitter, "error", activeMemoryItemId, "잘못된 memoryItemId 요청입니다. 다시 요청해주세요", "");
            safeComplete(emitter, isCompleted);
            return;
        }

        String presignedUrl = getPresignedUrl(currentItem.getFileKey());
        Map<String, List<String>> messageHistoryByRole = temporaryChatStore.getChatHistorySplitByRole(sessionId, memoryItemTempId);

        temporaryChatStore.addUserMessage(sessionId, memoryItemTempId, userMessage);

        boolean isEndCommand = PromptText.GENERATE_STORY.getText().equalsIgnoreCase(userMessage);
        boolean isThirdTurn = temporaryChatStore.getTurnCount(sessionId, memoryItemTempId) >= 3;

        if (isEndCommand || isThirdTurn) {
            handleStoryGeneration(sessionId, memory, currentItem, messageHistoryByRole, emitter, isCompleted);
        } else {
            flaskServer.sendMessage(sessionId, presignedUrl, userMessage, messageHistoryByRole, memoryItemTempId, wrapEmitter(emitter, isCompleted));
        }
    }

    private void handleStoryGeneration(String sessionId, Memory memory, MemoryItem currentItem,
            Map<String, List<String>> messageHistoryByRole, SseEmitter emitter, AtomicBoolean isCompleted) throws IOException {

        String presignedUrl = getPresignedUrl(currentItem.getFileKey());
        String story = flaskServer.generateDiaryFromChatAndImageUrl(sessionId, messageHistoryByRole, presignedUrl);
        currentItem.updateContent(story);

        TempMemoryDto updatedDto = TempMemoryDto.from(memory, memory.getMemoryItems().stream()
                .map(item -> new TempMemoryItemDto(item.getTempId(), getPresignedUrl(item.getFileKey()), item.getContent(), item.getSequence()))
                .toList());
        temporaryMemoryStore.save(sessionId, updatedDto);

        List<MemoryItem> sortedItems = memory.getMemoryItems().stream()
                .sorted(Comparator.comparingInt(MemoryItem::getSequence))
                .toList();

        int currentIndex = sortedItems.indexOf(currentItem);
        if (currentIndex + 1 < sortedItems.size()) {
            MemoryItem nextItem = sortedItems.get(currentIndex + 1);
            temporaryChatStore.initChat(sessionId, nextItem.getTempId());

            String nextPresignedUrl = getPresignedUrl(nextItem.getFileKey());
            flaskServer.initiateChatWithImageUrl(sessionId, nextPresignedUrl, nextItem.getTempId(), wrapEmitter(emitter, isCompleted));
        } else {
            emitter.send(SseEmitter.event().name("done").data(Map.of("type", "done", "message", "일기 생성이 완료되었습니다.")));
            safeComplete(emitter, isCompleted);
        }
    }

    // ------------------- Helper Methods -------------------

    private Memory loadMemory(String sessionId) {
        Memory memory = temporaryMemoryStore.load(sessionId);
        if (memory == null) throw new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION);
        return memory;
    }

    private MemoryItem getFirstMemoryItem(Memory memory) {
        return memory.getMemoryItems().stream()
                .sorted(Comparator.comparingInt(MemoryItem::getSequence))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION));
    }

    private MemoryItem getMemoryItemById(Memory memory, String tempId) {
        return memory.getMemoryItems().stream()
                .filter(item -> item.getTempId().equals(tempId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION));
    }

    private String getPresignedUrl(String fileKey) {
        return fileService.generatePresignedUrlToRead(fileKey).preSignedUrl();
    }

    private void sendEmitterPayload(SseEmitter emitter, String type, String tempId, String message, String presignedUrl) throws IOException {
        EmitterPayloadDto payload = new EmitterPayloadDto(tempId, type, message, presignedUrl);
        emitter.send(SseEmitter.event().name(type).data(payload));
    }

    private void safeComplete(SseEmitter emitter, AtomicBoolean isCompleted) {
        if (isCompleted.compareAndSet(false, true)) {
            emitter.complete();
        }
    }

    private void safeCompleteWithError(SseEmitter emitter, Throwable e, AtomicBoolean isCompleted) {
        if (isCompleted.compareAndSet(false, true)) {
            emitter.completeWithError(e);
        }
    }

    private SseEmitter wrapEmitter(SseEmitter emitter, AtomicBoolean isCompleted) {
        return new SseEmitter() {
            @Override
            public void send(Object object) throws IOException {
                if (!isCompleted.get()) {
                    emitter.send(object);
                }
            }

            @Override
            public void complete() {
                safeComplete(emitter, isCompleted);
            }

            @Override
            public void completeWithError(Throwable ex) {
                safeCompleteWithError(emitter, ex, isCompleted);
            }
        };
    }
}