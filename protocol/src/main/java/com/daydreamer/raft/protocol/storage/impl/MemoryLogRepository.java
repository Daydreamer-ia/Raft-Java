package com.daydreamer.raft.protocol.storage.impl;

import com.daydreamer.raft.protocol.entity.LogEntry;
import com.daydreamer.raft.protocol.storage.StorageRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Daydreamer
 * <p>
 * storage in memory
 */
public class MemoryLogRepository implements StorageRepository {
    
    /**
     * uncommitted logs
     */
    private List<LogEntry> uncommittedLog = Collections.synchronizedList(new ArrayList<>());
    
    /**
     * committed logs
     */
    private List<LogEntry> committedLog = Collections.synchronizedList(new ArrayList<>());
    
    @Override
    public void commit(int term, long logId) {
    
    }
    
    @Override
    public void append(LogEntry logEntry) {
    
    }
    
    @Override
    public LogEntry getLog(long logId) {
        return null;
    }
    
    @Override
    public long getLastCommittedLogId() {
        return 0;
    }
}
