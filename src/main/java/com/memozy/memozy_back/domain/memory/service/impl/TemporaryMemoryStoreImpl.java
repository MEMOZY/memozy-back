package com.memozy.memozy_back.domain.memory.service.impl;

import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.service.TemporaryMemoryStore;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class TemporaryMemoryStoreImpl implements TemporaryMemoryStore {

    private final Map<String, Memory> store = new ConcurrentHashMap<>() {};

    @Override
    public void save(String sessionId, Memory memory) {
        store.put(sessionId, memory);
    }

    @Override
    public Memory load(String sessionId) {
        return store.get(sessionId);
    }

    @Override
    public void remove(String sessionId) {
        store.remove(sessionId);
    }
}