package com.memozy.memozy_back.domain.memory.dto.request;

import com.memozy.memozy_back.domain.memory.constant.MemoryCategory;
import com.memozy.memozy_back.domain.memory.dto.MemoryItemDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record UpdateMemoryRequest(
        @NotBlank String title,
        @NotNull MemoryCategory category,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull List<MemoryItemDto> memoryItems,
        @Schema(description = "권한 부여에 대한 정보 목록 (비어있을 수 있음)")
        List<AccessGrantRequest> accesses,
        @NotNull String editLockToken
) {
}
