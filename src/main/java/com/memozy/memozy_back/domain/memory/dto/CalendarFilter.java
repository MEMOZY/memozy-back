package com.memozy.memozy_back.domain.memory.dto;

import com.drew.lang.annotations.Nullable;
import com.memozy.memozy_back.domain.memory.constant.MemoryCategory;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record CalendarFilter(
        @Nullable LocalDate from,
        @Nullable LocalDate to,
        @Nullable MemoryCategory memoryCategory
) {
}
