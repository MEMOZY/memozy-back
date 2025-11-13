package com.memozy.memozy_back.domain.memory.dto.request;

public record CreateEditLockResponse(
    String token,
    long ttl
) {
}
