package com.memozy.memozy_back.domain.gpt.controller;

import com.memozy.memozy_back.domain.gpt.dto.request.UserAnswerRequest;
import com.memozy.memozy_back.domain.gpt.dto.response.GetTempMemoryItems;
import com.memozy.memozy_back.domain.gpt.service.GptChatService;
import com.memozy.memozy_back.global.annotation.CurrentUserId;
import com.memozy.memozy_back.global.redis.SessionManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "대화 시작 (이미지 기반 초기 질문 생성)", description = "sessionId를 받아 초기 프롬프트 질문을 스트리밍으로 반환")
    @GetMapping(value = "/start", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter start(
            @CurrentUserId Long userId,
            @RequestParam String sessionId) {
        sessionManager.validateSessionOwner(userId, sessionId);
        SseEmitter emitter = new SseEmitter(120_000L);  // 2분 타임아웃
        gptChatService.generateInitialPrompts(sessionId, emitter);
        return emitter;
    }

    @Operation(summary = "유저 답변 처리", description = "sessionId와 유저 답변을 받아 GPT 응답을 스트리밍으로 반환")
    @PostMapping(value = "/answer", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter answerWithStream(
            @CurrentUserId Long userId,
            @RequestParam String sessionId,
            @RequestBody UserAnswerRequest request) {
        sessionManager.validateSessionOwner(userId, sessionId);
        SseEmitter emitter = new SseEmitter(120_000L);
        gptChatService.handleUserAnswer(sessionId, request, emitter);
        return emitter;
    }

    @Operation(summary = "최종 일기 생성", description = "최종 정리된 일기를 생성해 반환")
    @PostMapping("/final-diary")
    public ResponseEntity<GetTempMemoryItems> generateFinalDiary(
            @CurrentUserId Long userId,
            @RequestParam String sessionId) {
        sessionManager.validateSessionOwner(userId, sessionId);
        return ResponseEntity.ok(
                GetTempMemoryItems.from(
                        gptChatService.generateFinalDiaries(sessionId)
                )
        );
    }
}