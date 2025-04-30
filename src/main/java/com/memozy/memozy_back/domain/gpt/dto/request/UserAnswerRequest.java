package com.memozy.memozy_back.domain.gpt.dto.request;

public record UserAnswerRequest(
        Long memoryItemId,
        String userAnswer
) {}
