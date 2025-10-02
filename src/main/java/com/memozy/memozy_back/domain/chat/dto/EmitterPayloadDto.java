package com.memozy.memozy_back.domain.chat.dto;

public record EmitterPayloadDto(
        String memoryItemTempId,
        String type,
        String message,
        String presignedUrl
) {}