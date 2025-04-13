package com.memozy.memozy_back.domain.memory.dto.request;

import com.memozy.memozy_back.domain.memory.domain.MemoryCategory;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

public record UpdateMemoryRequest(
        @NotBlank String title,
        @NotBlank MemoryCategory category,
        LocalDate startDate,
        LocalDate endDate,
        List<Long>friendIds // 함께하는 친구들 (옵션)
        ) {
}
