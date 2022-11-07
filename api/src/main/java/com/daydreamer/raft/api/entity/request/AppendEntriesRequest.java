package com.daydreamer.raft.api.entity.request;

import com.daydreamer.raft.api.entity.Request;
import com.daydreamer.raft.api.entity.base.LogEntry;

/**
 * @author Daydreamer
 */
public class AppendEntriesRequest extends Request {
    
    /**
     * last committed log id
     */
    private long lastLogId;
    
    /**
     * last committed log term
     */
    private int lastTerm;
    
    /**
     * log
     */
    private LogEntry logEntry;
    
    public AppendEntriesRequest(long lastLogId, int lastTerm, LogEntry logEntry) {
        this.lastLogId = lastLogId;
        this.lastTerm = lastTerm;
        this.logEntry = logEntry;
    }
    
    public LogEntry getLogEntry() {
        return logEntry;
    }
    
    public void setLogEntry(LogEntry logEntry) {
        this.logEntry = logEntry;
    }
    
    public long getLastLogId() {
        return lastLogId;
    }
    
    public void setLastLogId(long lastLogId) {
        this.lastLogId = lastLogId;
    }
    
    public int getLastTerm() {
        return lastTerm;
    }
    
    public void setLastTerm(int lastTerm) {
        this.lastTerm = lastTerm;
    }
}
