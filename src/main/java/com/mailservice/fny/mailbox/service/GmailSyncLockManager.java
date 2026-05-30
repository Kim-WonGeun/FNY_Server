package com.mailservice.fny.mailbox.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class GmailSyncLockManager {

    private final ConcurrentMap<String, ReentrantLock> syncLocks = new ConcurrentHashMap<>();

    SyncLock acquire(String mailAccountId) {
        ReentrantLock syncLock = syncLocks.computeIfAbsent(mailAccountId, ignored -> new ReentrantLock());
        if (!syncLock.tryLock()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 해당 메일 계정의 동기화가 진행 중입니다.");
        }
        return new SyncLock(mailAccountId, syncLock);
    }

    public class SyncLock implements AutoCloseable {

        private final String mailAccountId;
        private final ReentrantLock syncLock;

        private SyncLock(String mailAccountId, ReentrantLock syncLock) {
            this.mailAccountId = mailAccountId;
            this.syncLock = syncLock;
        }

        @Override
        public void close() {
            syncLock.unlock();
            if (!syncLock.isLocked() && !syncLock.hasQueuedThreads()) {
                syncLocks.remove(mailAccountId, syncLock);
            }
        }
    }
}
