package com.daydreamer.raft.protocol.entity;

import com.daydreamer.raft.protocol.constant.LogType;

import java.io.Serializable;

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
    private long lodId;
    
    /**
     * data
     */
    private Object data;
    
    /**
     * operation to data
     */
    private LogType logType;
    
    public LogEntry(int term, long lodId, Object data, LogType logType) {
        this.term = term;
        this.lodId = lodId;
        this.data = data;
        this.logType = logType;
    }
    
    public LogEntry(int term, long lodId) {
        this.term = term;
        this.lodId = lodId;
    }
    
    public int getTerm() {
        return term;
    }
    
    public void setTerm(int term) {
        this.term = term;
    }
    
    public long getLodId() {
        return lodId;
    }
    
    public void setLodId(long lodId) {
        this.lodId = lodId;
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
    public String toString() {
        return "LogEntry{" + "term=" + term + ", lodId=" + lodId + ", data=" + data + ", logType=" + logType + '}';
    }
}
