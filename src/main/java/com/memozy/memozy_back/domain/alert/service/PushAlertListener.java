package com.memozy.memozy_back.domain.alert.service;

import com.memozy.memozy_back.domain.memory.dto.MemorySharedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushAlertListener {

    private final PushEnqueueService pushEnqueueService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMemoryShared(MemorySharedEvent event) {
        pushEnqueueService.enqueueSharedInfo(
                event
        );
    }

}