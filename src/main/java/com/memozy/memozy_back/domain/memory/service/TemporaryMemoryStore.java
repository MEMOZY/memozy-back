package com.memozy.memozy_back.domain.memory.service;

import com.memozy.memozy_back.domain.memory.domain.Memory;

public interface TemporaryMemoryStore {
    void save(String sessionId, Memory memory);
    Memory load(String sessionId);
    void remove(String sessionId);
}
