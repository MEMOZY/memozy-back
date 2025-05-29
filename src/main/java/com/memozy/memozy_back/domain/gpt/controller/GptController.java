package com.memozy.memozy_back.domain.gpt.controller;

import com.memozy.memozy_back.domain.gpt.dto.request.UserAnswerRequest;
import com.memozy.memozy_back.domain.gpt.dto.response.GetTempMemoryItems;
import com.memozy.memozy_back.domain.gpt.service.GptChatService;
import com.memozy.memozy_back.global.annotation.CurrentUserId;
import com.memozy.memozy_back.global.exception.BusinessException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import com.memozy.memozy_back.global.exception.ErrorResponse;
import com.memozy.memozy_back.global.redis.SessionManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "GPT API", description = "일기 생성 과정")
@RestController
@RequestMapping("/gpt/chats")
@RequiredArgsConstructor
public class GptController {

    private final GptChatService gptChatService;
    private final SessionManager sessionManager;

    @GetMapping(value = "/start", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter start(
            @CurrentUserId Long userId,
            @RequestParam String sessionId) {
        sessionManager.validateSessionOwner(userId, sessionId);
        SseEmitter emitter = new SseEmitter(300_000L);
        gptChatService.generateInitialPrompts(sessionId, emitter);
        return emitter;
    }

    @PostMapping(value = "/answer", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter answerWithStream(
            @CurrentUserId Long userId,
            @RequestParam String sessionId,
            @RequestBody UserAnswerRequest request) {
        sessionManager.validateSessionOwner(userId, sessionId);
        SseEmitter emitter = new SseEmitter(300_000L);
        gptChatService.handleUserAnswer(sessionId, request, emitter);
        return emitter;
    }

    @PostMapping("/final-diary")
    public ResponseEntity<GetTempMemoryItems> generateFinalDiary(
            @CurrentUserId Long userId,
            @RequestParam String sessionId) {
        sessionManager.validateSessionOwner(userId, sessionId);
        return ResponseEntity.ok(
                GetTempMemoryItems.from(
                        gptChatService.generateFinalDiarys(sessionId)
                )
        );
    }
}