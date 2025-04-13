package com.memozy.memozy_back.domain.memory.dto.request;

import com.memozy.memozy_back.domain.memory.domain.MemoryCategory;
import com.memozy.memozy_back.domain.memory.dto.MemoryItemDto;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

public record UpdateMemoryRequest(
        String title,
        MemoryCategory category,
        LocalDate startDate,
        LocalDate endDate,
        List<MemoryItemDto> memoryItems,
        List<Long> sharedUserIds
) {}
