package com.daydreamer.raft.protocol.storage;

import com.daydreamer.raft.protocol.entity.LogEntry;

/**
 * @author Daydreamer
 *
 * log storage
 */
public interface StorageRepository {
    
    /**
     * commit
     *
     * @param term term
     * @param logId log id
     */
    void commit(int term, long logId);
    
    /**
     * append log
     *
     * @param logEntry new log
     */
    void append(LogEntry logEntry);
    
    /**
     * get log by id
     *
     * @param logId log id
     * @return log
     */
    LogEntry getLog(long logId);
    
    /**
     * get last committed log id
     *
     * @return log id
     */
    long getLastCommittedLogId();
}
