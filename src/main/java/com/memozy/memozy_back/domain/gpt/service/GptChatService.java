package com.memozy.memozy_back.domain.gpt.service;

import com.memozy.memozy_back.domain.file.service.FileService;
import com.memozy.memozy_back.domain.gpt.constant.PromptText;
import com.memozy.memozy_back.domain.gpt.dto.ChatMessage;
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
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
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

        String presignedUrl = getPresignedUrl(currentItem.getFileKey());
        List<String> messageHistory = temporaryChatStore.getChatAll(sessionId, memoryItemTempId)
                .stream().map(ChatMessage::content).toList();

        String gptReply = flaskServer.sendMessage(sessionId, presignedUrl, userMessage, messageHistory);
        temporaryChatStore.addUserMessage(sessionId, memoryItemTempId, userMessage);
        temporaryChatStore.addAssistantMessage(sessionId, memoryItemTempId, gptReply);

        boolean isEndCommand = PromptText.GENERATE_STORY.getText().equalsIgnoreCase(userMessage);
        boolean isThirdTurn = temporaryChatStore.getUserMessageCount(sessionId, memoryItemTempId) >= 3;

        if (isEndCommand || isThirdTurn) {
            handleStoryGeneration(sessionId, memory, currentItem, messageHistory, emitter);
        } else {
            sendEmitterPayload(emitter, "reply", currentItem.getTempId(), gptReply, "");
        }
    }

    private void handleStoryGeneration(String sessionId, Memory memory, MemoryItem currentItem,
            List<String> messageHistory, SseEmitter emitter) throws IOException {
        String presignedUrl = getPresignedUrl(currentItem.getFileKey());
        String story = flaskServer.generateDiaryFromChatAndImageUrl(sessionId, messageHistory, presignedUrl);
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
            String nextQuestion = flaskServer.generateDiaryFromChatAndImageUrl(sessionId, messageHistory, nextPresignedUrl);
            temporaryChatStore.addAssistantMessage(sessionId, nextItem.getTempId(), nextQuestion);

            sendEmitterPayload(emitter, "question", nextItem.getTempId(), nextQuestion, nextPresignedUrl);
        } else {
            emitter.send(SseEmitter.event().name("done").data(Map.of(
                    "type", "done",
                    "message", "일기 생성이 완료되었습니다."
            )));
        }
    }

    private Memory loadMemory(String sessionId) {
        Memory memory = temporaryMemoryStore.load(sessionId);
        if (memory == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION);
        }
        return memory;
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