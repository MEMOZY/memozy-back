package com.memozy.memozy_back.domain.memory.repository.querydsl;

import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import java.util.List;
import java.util.Map;

public interface MemoryItemRepositoryCustom {
    Map<Long, MemoryItem> findFirstItemsByMemoryIds(List<Long> memoryIds);
}