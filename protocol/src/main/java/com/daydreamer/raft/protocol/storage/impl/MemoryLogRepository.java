package com.daydreamer.raft.protocol.storage.impl;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.protocol.constant.LogErrorCode;
import com.daydreamer.raft.protocol.exception.LogException;
import com.daydreamer.raft.protocol.storage.StorageRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Daydreamer
 * <p>
 * storage in memory. its max log id is Integer.MAX_VALUE - 1
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
    public synchronized boolean commit(int term, long logId) throws LogException {
        if (uncommittedLog.size() == 0) {
            throw new LogException(LogErrorCode.UNCOMMITTED_LOG_TO_LESS);
        }
        // if success
        long lastUncommittedLogId = uncommittedLog.get(uncommittedLog.size() - 1).getLodId();
        long lastCommittedLogId = committedLog.get(committedLog.size() - 1).getLodId();
        // update threshold
        if (lastUncommittedLogId < logId) {
            throw new LogException(LogErrorCode.UNCOMMITTED_LOG_TO_LESS);
        }
        // find index
        int tmp = uncommittedLog.size() - 1;
        while (tmp >= 0 && uncommittedLog.get(tmp).getLodId() != lastCommittedLogId) {
            tmp--;
        }
        int startIndex = tmp + 1;
        // commit until
        while (logId >= lastCommittedLogId) {
            // append
            committedLog.add(uncommittedLog.get(startIndex));
            lastCommittedLogId++;
            startIndex++;
        }
        return true;
    }
    
    @Override
    public synchronized boolean append(LogEntry logEntry) throws LogException {
        // if empty
        if (uncommittedLog.size() == 0) {
            // not first
            if (logEntry.getLodId() != 1) {
                return false;
            }
            uncommittedLog.add(logEntry);
            return true;
        }
        // judge last log
        LogEntry lastLog = uncommittedLog.get(uncommittedLog.size() - 1);
        if (lastLog.getLodId() + 1 == logEntry.getLodId()) {
            uncommittedLog.add(logEntry);
            return true;
        }
        // cover
        else if (lastLog.getLodId() + 1 > logEntry.getLodId()) {
            uncommittedLog.add((int) logEntry.getLodId() + 1, logEntry);
        }
        return false;
    }
    
    @Override
    public synchronized LogEntry getLog(long logId) {
        if (committedLog.size() < logId - 1) {
            return null;
        }
        return committedLog.get((int) logId);
    }
    
    @Override
    public synchronized long getLastCommittedLogId() {
        if (uncommittedLog.size() == 0) {
            return -1;
        }
        return uncommittedLog.get(committedLog.size() - 1).getLodId();
    }
    
    @Override
    public long getLastUncommittedLogId() {
        if (committedLog.size() == 0) {
            return -1;
        }
        return committedLog.get(committedLog.size() - 1).getLodId();
    }
}
