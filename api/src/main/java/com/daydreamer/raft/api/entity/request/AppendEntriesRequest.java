package com.daydreamer.raft.api.entity.request;

import com.daydreamer.raft.api.entity.Request;
import com.daydreamer.raft.api.entity.base.LogEntry;

import java.util.List;

/**
 * @author Daydreamer
 */
public class AppendEntriesRequest extends Request {
    
    /**
     * last committed log id
     */
    private long lastCommittedId;
    
    /**
     * last committed log term
     */
    private int lastTerm;
    
    /**
     * log from last committed log in follower to current committed log in leader
     */
    private List<LogEntry> logEntries;
    
    /**
     * current term
     */
    private int currentTerm;
    
    /**
     * current committed log id
     */
    private long currentCommittedLogId;
    
    /**
     * whether it is payload
     */
    private boolean payload;
    
    public List<LogEntry> getLogEntries() {
        return logEntries;
    }
    
    public void setLogEntries(List<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }
    
    public int getCurrentTerm() {
        return currentTerm;
    }
    
    public void setCurrentTerm(int currentTerm) {
        this.currentTerm = currentTerm;
    }
    
    public long getCurrentCommittedLogId() {
        return currentCommittedLogId;
    }
    
    public void setCurrentCommittedLogId(long currentCommittedLogId) {
        this.currentCommittedLogId = currentCommittedLogId;
    }
    
    public boolean isPayload() {
        return payload;
    }
    
    public void setPayload(boolean payload) {
        this.payload = payload;
    }
    
    public List<LogEntry> getLogEntry() {
        return logEntries;
    }
    
    public void setLogEntry(List<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }
    
    public long getLastCommittedId() {
        return lastCommittedId;
    }
    
    public void setLastCommittedId(long lastCommittedId) {
        this.lastCommittedId = lastCommittedId;
    }
    
    public int getLastTerm() {
        return lastTerm;
    }
    
    public void setLastTerm(int lastTerm) {
        this.lastTerm = lastTerm;
    }
}
