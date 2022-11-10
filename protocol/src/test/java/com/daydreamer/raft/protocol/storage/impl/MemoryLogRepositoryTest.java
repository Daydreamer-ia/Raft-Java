package com.daydreamer.raft.protocol.storage.impl;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.protocol.exception.LogException;
import com.daydreamer.raft.protocol.storage.StorageRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MemoryLogRepositoryTest {
    
    @Test
    void committedAppend() throws LogException {
        StorageRepository storageRepository = new MemoryLogRepository();
        LogEntry logEntry = new LogEntry(0, 0);
        Assertions.assertTrue(storageRepository.append(logEntry));
        logEntry = new LogEntry(0, 1);
        Assertions.assertTrue(storageRepository.append(logEntry));
        logEntry = new LogEntry(0, 2);
        Assertions.assertTrue(storageRepository.append(logEntry));
        logEntry = new LogEntry(0, 3);
        Assertions.assertTrue(storageRepository.append(logEntry));
        logEntry = new LogEntry(1, 4);
        Assertions.assertTrue(storageRepository.append(logEntry));
        // committed all
        Assertions.assertTrue(storageRepository.commit(1, 4));
        // committed again
        Assertions.assertTrue(storageRepository.commit(1, 4));
        
        // committed fail
        Assertions.assertFalse(storageRepository.commit(1, 5));
        
        // append more
        logEntry = new LogEntry(1, 5);
        Assertions.assertTrue(storageRepository.append(logEntry));
        logEntry = new LogEntry(1, 6);
        Assertions.assertTrue(storageRepository.append(logEntry));
        // committed all
        Assertions.assertTrue(storageRepository.commit(1, 6));
    }
    
    @Test
    void notCommittedAppend() throws LogException {
        StorageRepository storageRepository = new MemoryLogRepository();
        LogEntry logEntry = new LogEntry(0, 0);
        Assertions.assertTrue(storageRepository.append(logEntry));
        logEntry = new LogEntry(0, 1);
        Assertions.assertTrue(storageRepository.append(logEntry));
        logEntry = new LogEntry(0, 2);
        Assertions.assertTrue(storageRepository.append(logEntry));
        logEntry = new LogEntry(0, 3);
        Assertions.assertTrue(storageRepository.append(logEntry));
        logEntry = new LogEntry(1, 4);
        Assertions.assertTrue(storageRepository.append(logEntry));
        
        // append fail
        logEntry = new LogEntry(1, 6);
        Assertions.assertFalse(storageRepository.append(logEntry));
        logEntry = new LogEntry(1, 7);
        Assertions.assertFalse(storageRepository.append(logEntry));
        logEntry = new LogEntry(1, 8);
        Assertions.assertFalse(storageRepository.append(logEntry));
        
        // append true
        logEntry = new LogEntry(2, 5);
        Assertions.assertTrue(storageRepository.append(logEntry));
        logEntry = new LogEntry(2, 6);
        Assertions.assertTrue(storageRepository.append(logEntry));
        
        // cover
        logEntry = new LogEntry(1, 5);
        Assertions.assertTrue(storageRepository.append(logEntry));
        logEntry = new LogEntry(1, 6);
        Assertions.assertTrue(storageRepository.append(logEntry));
    }
}