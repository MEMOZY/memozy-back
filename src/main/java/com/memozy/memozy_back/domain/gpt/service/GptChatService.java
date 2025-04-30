package com.memozy.memozy_back.domain.gpt.service;

import com.memozy.memozy_back.domain.file.service.FileService;
import com.memozy.memozy_back.domain.gpt.constant.PromptText;
import com.memozy.memozy_back.domain.gpt.dto.ChatMessage;
import com.memozy.memozy_back.domain.gpt.dto.request.UserAnswerRequest;
import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import com.memozy.memozy_back.domain.memory.service.TemporaryMemoryStore;
import com.memozy.memozy_back.global.exception.BusinessException;
import com.memozy.memozy_back.global.exception.ErrorCode;
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
    private final GptChatStore gptChatStore;
    private final GptClient gptClient;

    public SseEmitter generateInitialPrompts(String sessionId, SseEmitter emitter) {
        Memory temporaryMemory = temporaryMemoryStore.load(sessionId);
        if (temporaryMemory == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION);
        }
        var memoryItems = temporaryMemory.getMemoryItems();

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                List<MemoryItem> sortedItems = memoryItems.stream()
                        .sorted(Comparator.comparingInt(MemoryItem::getSequence))
                        .toList();

                for (MemoryItem memoryItem : sortedItems) {
                    gptChatStore.initChat(memoryItem.getTempId());

                    String fileKey = memoryItem.getFileKey();
                    String base64Image = fileService.transferToBase64(fileKey);

                    String firstQuestion = gptClient.initiateChatWithImage(base64Image);
                    gptChatStore.addAssistantMessage(memoryItem.getTempId(), firstQuestion);

                    Map<String, Object> payload = Map.of(
                            "memoryItemTempId", memoryItem.getTempId(),
                            "type", "question",
                            "message", firstQuestion,
                            "imageUrl", fileService
                                    .generatePresignedUrlToRead(fileKey)
                                    .preSignedUrl()
                    );

                    emitter.send(SseEmitter.event()
                            .name("question")
                            .data(payload));
                }

                emitter.complete();

            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    public void handleUserAnswer(String sessionId, UserAnswerRequest request, SseEmitter emitter) {
        Long memoryItemTempId = request.memoryItemTempId();
        String userAnswer = request.userAnswer().trim();

        gptChatStore.addUserMessage(memoryItemTempId, userAnswer);

        MemoryItem memoryItem = temporaryMemoryStore.load(sessionId)
                .getMemoryItems().stream()
                .filter(item -> item.getTempId().equals(memoryItemTempId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION));

        String base64Image = fileService.transferToBase64(memoryItem.getFileKey());

        List<String> messageHistory = gptChatStore.getChat(memoryItemTempId).stream()
                .map(ChatMessage::content)
                .toList();

        String gptReply = gptClient.sendMessage(messageHistory);
        gptChatStore.addAssistantMessage(memoryItemTempId, gptReply);

        try {
            boolean isEndCommand = PromptText.GENERATE_STORY.getText().equalsIgnoreCase(userAnswer);
            boolean isThirdTurn = gptChatStore.getUserMessageCount(memoryItemTempId) >= 3;


            if (isEndCommand || isThirdTurn) { // 스토리(일기) 초안 생성
                String story = gptClient.generateStoryFromChatAndImage(messageHistory, base64Image);
                memoryItem.updateContent(story);
                gptChatStore.removeChat(memoryItemTempId);

                emitter.send(Map.of(
                        "memoryItemId", memoryItem.getTempId(),
                        "type", "story",
                        "message", story
                ));
            } else {
                emitter.send(Map.of(
                        "memoryItemId", memoryItem.getTempId(),
                        "type", "reply",
                        "message", gptReply
                ));
            }

            emitter.complete();

        } catch (Exception e) {
            emitter.completeWithError(e);
        } finally {
            emitter.complete(); // 항상 실행 보장
        }
    }
}