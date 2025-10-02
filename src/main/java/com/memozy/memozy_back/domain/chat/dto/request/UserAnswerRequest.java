package com.memozy.memozy_back.domain.chat.dto.request;

public record UserAnswerRequest(
        String memoryItemTempId,
        String userAnswer
) {}
