package com.memozy.memozy_back.domain.memory.dto;


import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.domain.MemoryCategory;
import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import com.memozy.memozy_back.domain.memory.domain.MemoryShared;
import java.time.LocalDate;
import java.util.List;

public record MemoryInfoDto(
        Long id,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        MemoryCategory category,
        List<MemoryItem> memoryItems,
        List<Long> sharedUserIds
) {
    public static MemoryInfoDto from(Memory memory) {
        return new MemoryInfoDto(
                memory.getId(),
                memory.getTitle(),
                memory.getStartDate(),
                memory.getEndDate(),
                memory.getCategory(),
                memory.getMemoryItems(),
                memory.getSharedUsers().stream()
                        .map(MemoryShared::getId)
                        .toList()
        );
    }
}
