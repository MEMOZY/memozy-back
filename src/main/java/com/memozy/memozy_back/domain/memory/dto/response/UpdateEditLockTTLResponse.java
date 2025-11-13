package com.memozy.memozy_back.domain.memory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateEditLockTTLResponse(

    @Schema(description = "락 토큰 유효시간(ms)")
    long ttl
) {
}
