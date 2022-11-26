package com.daydreamer.raft.api.entity.base;

import com.daydreamer.raft.api.entity.constant.LogType;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Daydreamer
 */
public class Payload implements Serializable {
    
    private Map<String, String> metadata;
    
    private LogType logType;
    
    public Payload() {
    }
    
    public Payload(Map<String, String> object, LogType logType) {
        this.metadata = object;
        this.logType = logType;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public LogType getLogType() {
        return logType;
    }
    
    public void setLogType(LogType logType) {
        this.logType = logType;
    }
}
