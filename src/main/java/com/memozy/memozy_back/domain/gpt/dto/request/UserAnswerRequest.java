package com.memozy.memozy_back.domain.gpt.dto.request;

public record UserAnswerRequest(
        String memoryItemTempId,
        String userAnswer
) {}
