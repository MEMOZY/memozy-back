package com.memozy.memozy_back.domain.memory.service;

import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.dto.TempMemoryDto;

public interface TemporaryMemoryStore {
    void save(String sessionId, TempMemoryDto tempMemoryDto);
    Memory load(String sessionId);
    void remove(String sessionId);
}
