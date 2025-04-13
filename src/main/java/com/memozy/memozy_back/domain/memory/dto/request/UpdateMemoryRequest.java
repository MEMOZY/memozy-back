package com.memozy.memozy_back.domain.memory.dto.request;

import com.memozy.memozy_back.domain.memory.domain.MemoryCategory;
import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import com.memozy.memozy_back.domain.memory.dto.MemoryItemDto;
import com.memozy.memozy_back.domain.user.domain.User;
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
        List<Long> sharedUsersId
) {}
