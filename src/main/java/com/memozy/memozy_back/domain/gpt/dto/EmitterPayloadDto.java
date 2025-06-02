package com.memozy.memozy_back.domain.gpt.dto;

public record EmitterPayloadDto(
        String memoryItemTempId,
        String type,
        String message,
        String presignedUrl
) {}