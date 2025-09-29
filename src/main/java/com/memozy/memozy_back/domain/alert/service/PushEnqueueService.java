package com.memozy.memozy_back.domain.alert.service;

import com.memozy.memozy_back.domain.memory.dto.MemorySharedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PushEnqueueService {

    private final ExpoPushSender expoPushSender;

    @Async
    public void enqueueSharedInfo(MemorySharedEvent memorySharedEvent) {
        expoPushSender.sendSharedInfo(memorySharedEvent);
    }

}