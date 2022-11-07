package com.daydreamer.raft.api.entity.request;

import com.daydreamer.raft.api.entity.Request;
import com.daydreamer.raft.api.entity.base.LogEntry;

import java.util.List;

/**
 * @author Daydreamer
 */
public class AppendEntriesRequest extends Request {
    
    /**
     * last committed or uncommitted log id
     */
    private long lastLogId;
    
    /**
     * last committed or uncommitted log term
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
     * current committed or uncommitted log id
     */
    private long currentLogId;
    
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
    
    public long getCurrentLogId() {
        return currentLogId;
    }
    
    public void setCurrentLogId(long currentLogId) {
        this.currentLogId = currentLogId;
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
