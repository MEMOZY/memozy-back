package com.memozy.memozy_back.domain.memory.repository.querydsl;

import com.memozy.memozy_back.domain.memory.domain.Memory;
import java.util.List;

public interface MemoryRepositoryCustom {
    List<Memory> findMemoriesByKeyword(String keyword);
}
