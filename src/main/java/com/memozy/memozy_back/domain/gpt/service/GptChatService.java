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
import java.io.IOException;
import java.time.Instant;
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

    public void generateInitialPrompts(String sessionId, SseEmitter emitter) {
        Memory temporaryMemory = temporaryMemoryStore.load(sessionId);
        if (temporaryMemory == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION);
        }
        List<MemoryItem> sortedMemoryItems = temporaryMemory.getMemoryItems()
                .stream()
                .sorted(Comparator.comparingInt(MemoryItem::getSequence))
                .toList();;

        MemoryItem firstItem = sortedMemoryItems.get(0);
        String fileKey = firstItem.getFileKey();


        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("init")
                        .data(Map.of(
                                        "memoryItemTempId", firstItem.getTempId(),
                                        "type", "init",
                                        "message", "잠시만 기다려주세요."

                                )
                        )
                );

                gptChatStore.initChat(firstItem.getTempId());

                String base64Image = fileService.transferToBase64(fileKey);
                String firstQuestion = gptClient.initiateChatWithImage(base64Image);
                System.out.println("firstQuestion = " + firstQuestion);
                gptChatStore.addAssistantMessage(firstItem.getTempId(), firstQuestion);

                System.out.println("addmessage");
                Map<String, Object> payload = Map.of(
                        "memoryItemTempId", firstItem.getTempId(),
                        "type", "question",
                        "message", firstQuestion,
                        "presignedUrl", fileService.generatePresignedUrlToRead(fileKey).preSignedUrl()
                );
                System.out.println("payload = " + payload);

                emitter.send(SseEmitter.event().name("question").data(payload));
                System.out.println("send");
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                emitter.complete();
            }
        });
    }

    public void handleUserAnswer(String sessionId, UserAnswerRequest request, SseEmitter emitter) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                String memoryItemTempId = request.memoryItemTempId();
                String userAnswer = request.userAnswer().trim();

                gptChatStore.addUserMessage(memoryItemTempId, userAnswer);

                Memory memory = temporaryMemoryStore.load(sessionId);
                List<MemoryItem> sortedMemoryItems = memory.getMemoryItems().stream()
                        .sorted(Comparator.comparingInt(MemoryItem::getSequence))
                        .toList();

                MemoryItem currentItem = sortedMemoryItems.stream()
                        .filter(item -> item.getTempId().equals(memoryItemTempId))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION));

                String base64Image = fileService.transferToBase64(currentItem.getFileKey());
                List<String> messageHistory = gptChatStore.getChat(memoryItemTempId).stream()
                        .map(ChatMessage::content)
                        .toList();

                String gptReply = gptClient.sendMessage(messageHistory);
                gptChatStore.addAssistantMessage(memoryItemTempId, gptReply);

                boolean isEndCommand = PromptText.GENERATE_STORY.getText().equalsIgnoreCase(userAnswer);
                boolean isThirdTurn = gptChatStore.getUserMessageCount(memoryItemTempId) >= 3;

                if (isEndCommand || isThirdTurn) {
                    String story = gptClient.generateStoryFromChatAndImage(messageHistory, base64Image);
                    currentItem.updateContent(story);
                    gptChatStore.removeChat(memoryItemTempId);

                    int currentIndex = sortedMemoryItems.indexOf(currentItem);
                    boolean hasNext = currentIndex + 1 < sortedMemoryItems.size();

                    if (hasNext) {
                        MemoryItem nextItem = sortedMemoryItems.get(currentIndex + 1);
                        gptChatStore.initChat(nextItem.getTempId());

                        String nextBase64 = fileService.transferToBase64(nextItem.getFileKey());
                        String nextQuestion = gptClient.initiateChatWithImage(nextBase64);
                        gptChatStore.addAssistantMessage(nextItem.getTempId(), nextQuestion);

                        Map<String, Object> payload = Map.of(
                                "memoryItemTempId", nextItem.getTempId(),
                                "type", "question",
                                "message", nextQuestion,
                                "presignedUrl", fileService.generatePresignedUrlToRead(nextItem.getFileKey()).preSignedUrl()
                        );

                        emitter.send(SseEmitter.event().name("question").data(payload));
                    } else {
                        emitter.send(SseEmitter
                                .event()
                                .name("done")
                                .data(Map.of(
                                        "type", "done",
                                        "message", "일기 생성이 완료되었습니다.")));
                    }
                } else {
                    Map<String, Object> payload = Map.of(
                            "memoryItemId", currentItem.getTempId(),
                            "type", "reply",
                            "message", gptReply
                    );
                    emitter.send(SseEmitter.event().name("reply").data(payload));
                }

            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                emitter.complete();
            }
        });
    }
}
