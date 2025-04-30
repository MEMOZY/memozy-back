package com.memozy.memozy_back.domain.gpt.controller;

import com.memozy.memozy_back.domain.gpt.dto.request.UserAnswerRequest;
import com.memozy.memozy_back.domain.gpt.service.GptChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/gpt/chats")
@RequiredArgsConstructor
public class GptController {

    private final GptChatService gptChatService;

    @GetMapping("/conversations/start")
    public SseEmitter start(@RequestParam String sessionId) {
        SseEmitter emitter = new SseEmitter(300_000L);
        return gptChatService.generateInitialPrompts(sessionId, emitter);
    }

    @PostMapping(value = "/answer", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter answerWithStream(
            @RequestParam String sessionId,
            @RequestBody UserAnswerRequest request
    ) {
        SseEmitter emitter = new SseEmitter(300_000L); // 응답 대기 시간

        gptChatService.handleUserAnswer(sessionId, request, emitter); // emitter 넘겨주기

        return emitter;
    }
}