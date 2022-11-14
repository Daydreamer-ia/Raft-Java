package com.daydreamer.raft.protocol.storage.impl;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.protocol.exception.LogException;
import com.daydreamer.raft.protocol.storage.ReplicatedStateMachine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Daydreamer
 * <p>
 * storage in memory. its max log id is Integer.MAX_VALUE - 1
 */
public class MemoryReplicatedStateMachine implements ReplicatedStateMachine {
    
    /**
     * uncommitted logs
     */
    private List<LogEntry> logEntriesList = Collections.synchronizedList(new ArrayList<>());
    
    /**
     * last committed log
     */
    private int lastCommittedLogIndex = -1;
    
    /**
     * last committed log id
     */
    private long lastCommittedLogId = -1;
    
    /**
     * last uncommitted log id
     */
    private long lastUncommittedLogId = -1;
    
    /**
     * last uncommitted log index
     */
    private int lastUncommittedLogIndex = -1;
    
    @Override
    public synchronized boolean commit(int term, long logId) throws LogException {
        // no uncommitted log
        if (lastUncommittedLogId == -1) {
            return false;
        }
        // has committed
        if (lastCommittedLogId == logId) {
            return true;
        }
        // get last uncommitted log
        LogEntry lastUncommittedLog = logEntriesList.get(lastUncommittedLogIndex);
        // if log id smaller, normal
        if (lastUncommittedLog.getLogId() >= logId) {
            int committedLogIndex = lastCommittedLogIndex;
            committedLogIndex++;
            // find log
            while (logEntriesList.get(committedLogIndex).getLogId() != logId) {
                committedLogIndex++;
            }
            lastCommittedLogId = logEntriesList.get(committedLogIndex).getLogId();
            lastCommittedLogIndex = committedLogIndex;
            return true;
        }
        // need more log
        return false;
    }
    
    @Override
    public synchronized boolean append(LogEntry logEntry) throws LogException {
        // if empty
        if (lastUncommittedLogIndex == -1) {
            logEntriesList.add(logEntry);
            lastUncommittedLogIndex++;
            lastUncommittedLogId = logEntry.getLogId();
            return true;
        }
        // find suitable index
        if (logEntry.getLogId() <= lastCommittedLogId) {
            throw new IllegalArgumentException(
                    "log id: " + logEntry.getLogId() + " has committed, which cannot be modified!");
        }
        // not linked
        if (logEntry.getLogId() > lastUncommittedLogId + 1) {
            return false;
        }
        int index = lastUncommittedLogIndex;
        while (index > lastCommittedLogIndex
                && logEntriesList.get(index).getLogId() + 1 != logEntry.getLogId()) {
            index--;
        }
        if (index == lastUncommittedLogIndex) {
            logEntriesList.add(logEntry);
            lastUncommittedLogId = logEntry.getLogId();
            lastUncommittedLogIndex = index + 1;
        } else if (index < lastUncommittedLogIndex) {
            // remove old
            logEntriesList.add(index + 1, logEntry);
            logEntriesList.remove(index + 2);
        }
        return true;
    }
    
    @Override
    public synchronized LogEntry getCommittedLog(long logId) {
        // not found
        if (lastCommittedLogIndex == -1) {
            return null;
        }
        int index = lastCommittedLogIndex;
        while (index >= 0 && logEntriesList.get(index).getLogId() != logId) {
            index--;
        }
        return index == -1 ? null : logEntriesList.get(index);
    }
    
    @Override
    public LogEntry getUncommittedLog(long logId) {
        if (logEntriesList.size() == 0) {
            return null;
        }
        int index = logEntriesList.size() - 1;
        // if not committed anything
        if (lastCommittedLogIndex == -1) {
            while (index >= 0 && logEntriesList.get(index).getLogId() != logId) {
                index--;
            }
            return index == -1 ? null : logEntriesList.get(index);
        }
        // if has committed logs
        else {
            while (index > lastCommittedLogIndex && logEntriesList.get(index).getLogId() != logId) {
                index--;
            }
            return index == lastCommittedLogIndex ? null : logEntriesList.get(index);
        }
    }
    
    @Override
    public synchronized long getLastCommittedLogId() {
        return lastCommittedLogId;
    }
    
    @Override
    public LogEntry getLogById(long logId) {
        if (lastUncommittedLogIndex == -1) {
            return null;
        }
        if (lastUncommittedLogId >= logId) {
            int index = lastUncommittedLogIndex;
            while (index >= 0 && logEntriesList.get(index).getLogId() != logId) {
                index--;
            }
            return index == -1 ? null : logEntriesList.get(index);
        }
        return null;
    }
    
    @Override
    public synchronized long getLastUncommittedLogId() {
        return lastUncommittedLogId;
    }
    
    @Override
    public void close() {
        // nothing to do
    }
}
