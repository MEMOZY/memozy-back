package com.memozy.memozy_back.domain.memory.service;

import com.memozy.memozy_back.domain.memory.dto.MemoryEditedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryEventListener {

    private final MemoryEditLockService memoryEditLockService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMemoryEdited(MemoryEditedEvent event) {
        try {
            memoryEditLockService.release(
                    event.memoryId(),
                    event.userId(),
                    event.token()
            );
        } catch (Exception e) {
            log.warn("Failed to release edit lock. memoryId={}", event.memoryId(), e);
        }
    }
}