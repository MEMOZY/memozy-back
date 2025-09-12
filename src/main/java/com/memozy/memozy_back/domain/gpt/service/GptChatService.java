package com.memozy.memozy_back.domain.gpt.service;

import com.memozy.memozy_back.domain.file.service.FileService;
import com.memozy.memozy_back.domain.gpt.constant.PromptText;
import com.memozy.memozy_back.domain.gpt.dto.EmitterPayloadDto;
import com.memozy.memozy_back.domain.gpt.dto.request.UserAnswerRequest;
import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import com.memozy.memozy_back.domain.memory.dto.TempMemoryDto;
import com.memozy.memozy_back.domain.memory.dto.TempMemoryItemDto;
import com.memozy.memozy_back.domain.memory.repository.MemoryRepository;
import com.memozy.memozy_back.domain.user.domain.User;
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
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@Slf4j
@RequiredArgsConstructor
public class GptChatService {

    private final TemporaryMemoryStore temporaryMemoryStore;
    private final FileService fileService;
    private final TemporaryChatStore temporaryChatStore;
    private final FlaskServer flaskServer;

    private final TaskExecutor gptExecutor;
    private final MemoryRepository memoryRepository;

    public void generateInitialPrompts(String sessionId, SseEmitter emitter) {
        AtomicBoolean isCompleted = new AtomicBoolean(false);

        gptExecutor.execute(() -> {
            try {
                Memory memory = loadMemory(sessionId);
                MemoryItem firstItem = getFirstMemoryItem(memory);

                temporaryChatStore.initSession(sessionId);
                temporaryChatStore.initChat(sessionId, firstItem.getTempId());

                String presignedUrl = getPresignedUrl(firstItem.getFileKey());

                sendEmitterPayload(emitter, "first-question", firstItem.getTempId(), "첫 이미지 질문을 시작합니다.", presignedUrl);

                gptExecutor.execute(() -> {
                    flaskServer.initiateChatWithImageUrl(
                            sessionId, presignedUrl, firstItem.getTempId(),
                            emitter,
                            () -> safeComplete(emitter, isCompleted)
                    );
                });
            } catch (Exception e) {
                log.error("Initial prompt 처리 중 오류", e);
                safeCompleteWithError(emitter, e, isCompleted);
            }
        });
    }

    public void handleUserAnswer(String sessionId, UserAnswerRequest request, SseEmitter emitter, Long userId) {
        AtomicBoolean isCompleted = new AtomicBoolean(false);

        gptExecutor.execute(() -> {
            try {
                handleAnswerLogic(sessionId, request, emitter, isCompleted, userId);
            } catch (Exception e) {
                log.error("User answer 처리 중 오류", e);
                safeCompleteWithError(emitter, e, isCompleted);
            }
        });
    }

    private void handleAnswerLogic(String sessionId, UserAnswerRequest request, SseEmitter emitter,
            AtomicBoolean isCompleted, Long userId)
            throws IOException {
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
            handleStoryGeneration(sessionId, memory, currentItem, messageHistoryByRole, emitter, isCompleted, userId);
        } else {
            sendEmitterPayload(emitter, "open-reply", memoryItemTempId, "응답을 시작합니다.", presignedUrl);

            gptExecutor.execute(() -> {
                flaskServer.sendMessage(
                        sessionId, presignedUrl, userMessage, messageHistoryByRole,
                        memoryItemTempId, emitter,
                        () -> safeComplete(emitter, isCompleted)
                );
            });
        }
    }

    private void handleStoryGeneration(String sessionId, Memory memory, MemoryItem currentItem,
            Map<String, List<String>> messageHistoryByRole, SseEmitter emitter, AtomicBoolean isCompleted, Long ownerId) throws IOException {

        String presignedUrl = getPresignedUrl(currentItem.getFileKey());

        List<String> pastDiary = buildPastDiary(ownerId);
        String story = flaskServer.generateDiaryFromChatAndImageUrl(sessionId, messageHistoryByRole, presignedUrl, pastDiary);
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
            sendEmitterPayload(emitter, "open-question", nextItem.getTempId(), "다음 이미지 질문을 시작합니다.", nextPresignedUrl);

            gptExecutor.execute(() -> {
                flaskServer.initiateChatWithImageUrl(
                        sessionId, nextPresignedUrl, nextItem.getTempId(),
                        emitter,
                        () -> safeComplete(emitter, isCompleted)
                );
            });
        } else {
            sendEmitterPayload(emitter, "final-done", "", "모든 이미지에 대한 질문이 완료되었습니다.", "");
            safeComplete(emitter, isCompleted);
        }
    }

    /*
     ** 최종 일기 생성 요청을 Flask 서버에 보내고, 결과를 받아서 임시 메모리에 저장합니다.
     */
    public List<TempMemoryItemDto> generateFinalDiaries(String sessionId) {
        Memory tempMemory = loadMemory(sessionId);

        List<Map<String, String>> diaryList = buildDiaryRequestList(tempMemory);

        List<Map<String, String>> improvedDiaries = flaskServer.generateFinalDiaries(sessionId, diaryList);

        List<TempMemoryItemDto> updatedItems = improvedDiaries.stream()
                .map(entry -> buildUpdatedItem(entry, tempMemory))
                .toList();

        TempMemoryDto updatedDto = new TempMemoryDto(tempMemory.getOwner().getId(), updatedItems);
        temporaryMemoryStore.save(sessionId, updatedDto);

        return updatedItems;
    }



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

    private List<Map<String, String>> buildDiaryRequestList(Memory tempMemory) {
        return tempMemory.getMemoryItems().stream()
                .sorted(Comparator.comparingInt(MemoryItem::getSequence)) // sequence 오름차순 정렬
                .map(item -> Map.of(
                        "caption_id", item.getTempId(),
                        "caption", item.getContent()
                ))
                .toList();
    }

    private TempMemoryItemDto buildUpdatedItem(Map<String, String> entry, Memory tempMemory) {
        String captionId = entry.get("caption_id");
        String improvedCaption = entry.get("caption");

        if (entry.containsKey("warning")) {
            log.warn("향상된 일기 생성 실패 - {}", entry.get("warning"));
        }

        MemoryItem originalItem = findMemoryItemByTempId(tempMemory, captionId);

        return new TempMemoryItemDto(
                originalItem.getTempId(),
                getPresignedUrl(originalItem.getFileKey()),
                improvedCaption,
                originalItem.getSequence()
        );
    }

    private MemoryItem findMemoryItemByTempId(Memory tempMemory, String tempId) {
        return tempMemory.getMemoryItems().stream()
                .filter(item -> item.getTempId().equals(tempId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION));
    }


    private String concatDiaryText(Memory memory) {
        return memory.getMemoryItems().stream()
                .filter(it -> it.getContent() != null && !it.getContent().isBlank())
                .sorted(Comparator.comparing(MemoryItem::getSequence, Comparator.nullsLast(Integer::compareTo)))
                .map(MemoryItem::getContent)
                .collect(Collectors.joining("\n\n")); // 문단 간 공백
    }


    List<String> buildPastDiary(Long ownerId) {
        var latest = memoryRepository.findLatestWithItemsByOwner(ownerId, PageRequest.of(0,1))
                .stream().findFirst();
        if (latest.isEmpty()) return List.of();

        String oneDiary = concatDiaryText(latest.get());
        if (oneDiary.isBlank()) return List.of();

        return List.of(oneDiary); // ✅ 리스트 안에 문자열 1개
    }
}