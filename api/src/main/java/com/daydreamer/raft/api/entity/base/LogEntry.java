package com.daydreamer.raft.api.entity.base;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Daydreamer
 */
public class LogEntry implements Serializable {
    
    /**
     * term
     */
    private int term;
    
    /**
     * log id
     */
    private long logId;
    
    /**
     * data
     */
    private Payload payload;
    
    public LogEntry(int term, long logId, Payload payload) {
        this.term = term;
        this.logId = logId;
        this.payload = payload;
    }
    
    public LogEntry(int term, long logId) {
        this.term = term;
        this.logId = logId;
    }
    
    public int getTerm() {
        return term;
    }
    
    public void setTerm(int term) {
        this.term = term;
    }
    
    public long getLogId() {
        return logId;
    }
    
    public void setLogId(long logId) {
        this.logId = logId;
    }
    
    public Payload getPayload() {
        return payload;
    }
    
    public void setPayload(Payload payload) {
        this.payload = payload;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LogEntry)) {
            return false;
        }
        LogEntry logEntry = (LogEntry) o;
        return term == logEntry.term && logId == logEntry.logId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(term, logId);
    }
    
    @Override
    public String toString() {
        return "LogEntry{" + "term=" + term + ", logId=" + logId + ", log type=" + payload.getLogType() + '}';
    }
}
