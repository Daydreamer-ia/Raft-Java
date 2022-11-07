package com.daydreamer.raft.api.entity.base;

import com.daydreamer.raft.api.entity.constant.LogType;

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
    private Object data;
    
    /**
     * operation to data
     */
    private LogType logType;
    
    public LogEntry(int term, long logId, Object data, LogType logType) {
        this.term = term;
        this.logId = logId;
        this.data = data;
        this.logType = logType;
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
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public LogType getLogType() {
        return logType;
    }
    
    public void setLogType(LogType logType) {
        this.logType = logType;
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
        return "LogEntry{" + "term=" + term + ", lodId=" + logId + ", data=" + data + ", logType=" + logType + '}';
    }
}
