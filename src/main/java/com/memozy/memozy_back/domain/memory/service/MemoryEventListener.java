package com.memozy.memozy_back.domain.memory.service;

import com.memozy.memozy_back.domain.memory.dto.event.MemoryCreatedEvent;
import com.memozy.memozy_back.domain.memory.dto.event.MemoryEditedEvent;
import com.memozy.memozy_back.global.redis.SessionManager;
import com.memozy.memozy_back.global.redis.TemporaryChatStore;
import com.memozy.memozy_back.global.redis.TemporaryMemoryStore;
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

    private final SessionManager sessionManager;
    private final TemporaryMemoryStore temporaryMemoryStore;
    private final TemporaryChatStore temporaryChatStore;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MemoryCreatedEvent event) {
        clearTempMemoryOnRedis(event.sessionId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMemoryEdited(MemoryEditedEvent event) {
        try {
            memoryEditLockService.release(
                    event.memoryId(),
                    event.userId(),
                    event.token()
            );
        } catch (Exception e) {
            log.warn("[FAIL] 수정 락 해제에 실패했습니다. memoryId={}", event.memoryId(), e);
        }
    }

    private void clearTempMemoryOnRedis(String sessionId) {
        sessionManager.removeSession(sessionId);
        temporaryMemoryStore.remove(sessionId);
        temporaryChatStore.removeSession(sessionId);
    }
}