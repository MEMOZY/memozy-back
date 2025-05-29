package com.memozy.memozy_back.domain.gpt.service;

import com.memozy.memozy_back.domain.file.service.FileService;
import com.memozy.memozy_back.domain.gpt.constant.PromptText;
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
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public void generateInitialPrompts(String sessionId, SseEmitter emitter) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                handleInitialPrompt(sessionId, emitter);
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                emitter.complete();
            }
        });
    }

    private void handleInitialPrompt(String sessionId, SseEmitter emitter) throws IOException {
        Memory memory = loadMemory(sessionId);
        MemoryItem firstItem = getFirstMemoryItem(memory);

        temporaryChatStore.initSession(sessionId);
        temporaryChatStore.initChat(sessionId, firstItem.getTempId());

        String presignedUrl = getPresignedUrl(firstItem.getFileKey());
        String firstQuestion = flaskServer.initiateChatWithImageUrl(sessionId, presignedUrl);

        temporaryChatStore.addAssistantMessage(sessionId, firstItem.getTempId(), firstQuestion);

        sendEmitterPayload(emitter, "question", firstItem.getTempId(), firstQuestion, presignedUrl);
    }

    public void handleUserAnswer(String sessionId, UserAnswerRequest request, SseEmitter emitter) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                handleAnswerLogic(sessionId, request, emitter);
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                emitter.complete();
            }
        });
    }

    private void handleAnswerLogic(String sessionId, UserAnswerRequest request, SseEmitter emitter) throws IOException {
        String memoryItemTempId = request.memoryItemTempId();
        String userMessage = request.userAnswer().trim();

        Memory memory = loadMemory(sessionId);
        MemoryItem currentItem = getMemoryItemById(memory, memoryItemTempId);

        String activeMemoryItemId = temporaryChatStore.getActiveMemoryItemId(sessionId);
        if (!memoryItemTempId.equals(activeMemoryItemId)) {
            log.warn("잘못된 memoryItemId 요청: expected={}, received={}", activeMemoryItemId, memoryItemTempId);

            // error payload에 올바른 memoryItemTempId를 같이 담아 보냄
            sendEmitterPayload(emitter, "error", activeMemoryItemId,
                    "잘못된 memoryItemId 요청입니다. 다시 요청해주세요",
                    "");
            emitter.complete();
            return;
        }
        String presignedUrl = getPresignedUrl(currentItem.getFileKey());

        // 역할별로 분리된 history 가져오기
        Map<String, List<String>> messageHistoryByRole = temporaryChatStore.getChatHistorySplitByRole(sessionId, memoryItemTempId);

        String gptReply = flaskServer.sendMessage(sessionId, presignedUrl, userMessage, messageHistoryByRole);

        temporaryChatStore.addUserMessage(sessionId, memoryItemTempId, userMessage);
        temporaryChatStore.addAssistantMessage(sessionId, memoryItemTempId, gptReply);

        boolean isEndCommand = PromptText.GENERATE_STORY.getText().equalsIgnoreCase(userMessage);
        boolean isThirdTurn = temporaryChatStore.getTurnCount(sessionId, memoryItemTempId) >= 3;

        if (isEndCommand || isThirdTurn) {
            messageHistoryByRole = temporaryChatStore.getChatHistorySplitByRole(sessionId, memoryItemTempId); // 마지막 유저 응답
            handleStoryGeneration(sessionId, memory, currentItem, messageHistoryByRole, emitter);
        } else {
            sendEmitterPayload(emitter, "reply", currentItem.getTempId(), gptReply, "");
        }
    }

    private void handleStoryGeneration(String sessionId, Memory memory, MemoryItem currentItem,
            Map<String, List<String>> messageHistoryByRole, SseEmitter emitter) throws IOException {

        String presignedUrl = getPresignedUrl(currentItem.getFileKey());
        String story = flaskServer.generateDiaryFromChatAndImageUrl(sessionId, messageHistoryByRole, presignedUrl);
        currentItem.updateContent(story);

        TempMemoryDto updatedDto = TempMemoryDto.from(memory,
                memory.getMemoryItems().stream()
                        .map(item -> new TempMemoryItemDto(
                                item.getTempId(),
                                getPresignedUrl(item.getFileKey()),
                                item.getContent(),
                                item.getSequence()))
                        .toList());

        temporaryMemoryStore.save(sessionId, updatedDto);

        List<MemoryItem> sortedItems = memory.getMemoryItems().stream()
                .sorted(Comparator.comparingInt(MemoryItem::getSequence))
                .toList();

        int currentIndex = sortedItems.indexOf(currentItem);
        boolean hasNextMemoryItem = currentIndex + 1 < sortedItems.size();
        if (hasNextMemoryItem) {
            MemoryItem nextItem = sortedItems.get(currentIndex + 1);
            temporaryChatStore.initChat(sessionId, nextItem.getTempId());

            String nextPresignedUrl = getPresignedUrl(nextItem.getFileKey());

            // 다음 질문 뽑을 때 initiateChatWithImageUrl 사용
            String nextQuestion = flaskServer.initiateChatWithImageUrl(sessionId, nextPresignedUrl);

            temporaryChatStore.addAssistantMessage(sessionId, nextItem.getTempId(), nextQuestion);

            sendEmitterPayload(emitter, "question", nextItem.getTempId(), nextQuestion, nextPresignedUrl);
        } else {
            emitter.send(SseEmitter.event().name("done").data(Map.of(
                    "type", "done",
                    "message", "일기 생성이 완료되었습니다."
            )));
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

    private MemoryItem getFirstMemoryItem(Memory memory) {
        return memory.getMemoryItems().stream()
                .sorted(Comparator.comparingInt(MemoryItem::getSequence))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION));
    }

    private MemoryItem getMemoryItemById(Memory memory, String memoryItemTempId) {
        return memory.getMemoryItems().stream()
                .filter(item -> item.getTempId().equals(memoryItemTempId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION));
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

    private Memory loadMemory(String sessionId) {
        Memory memory = temporaryMemoryStore.load(sessionId);
        if (memory == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION);
        }
        return memory;
    }


    private String getPresignedUrl(String fileKey) {
        return fileService.generatePresignedUrlToRead(fileKey).preSignedUrl();
    }


    private void sendEmitterPayload(SseEmitter emitter, String type, String memoryItemTempId,
            String message, String presignedUrl) throws IOException {
        Map<String, Object> payload = Map.of(
                "memoryItemTempId", memoryItemTempId,
                "type", type,
                "message", message,
                "presignedUrl", presignedUrl
        );
        emitter.send(SseEmitter.event().name(type).data(payload));
    }


}