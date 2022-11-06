package com.daydreamer.raft.protocol.storage;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.protocol.exception.LogException;

/**
 * @author Daydreamer
 * <p>
 * log storage
 */
public interface StorageRepository {
    
    /**
     * commit
     *
     * @param term  term
     * @param logId log id
     * @return whether success, fail if new log id is not linked to last log id
     * @throws LogException LogException
     */
    boolean commit(int term, long logId) throws LogException;
    
    /**
     * append log
     *
     * @param logEntry new log
     * @return whether success, fail if new log id is not linked to last log id
     * @throws LogException LogException
     */
    boolean append(LogEntry logEntry) throws LogException;
    
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
    
    /**
     * get last uncommitted log id
     *
     * @return last uncommitted log id
     */
    long getLastUncommittedLogId();
}
