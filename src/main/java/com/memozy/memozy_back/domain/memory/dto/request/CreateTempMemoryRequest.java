package com.memozy.memozy_back.domain.memory.dto.request;

import com.memozy.memozy_back.domain.memory.dto.MemoryItemDto;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateTempMemoryRequest(
        @NotNull List<MemoryItemDto> memoryItems
) {
}
