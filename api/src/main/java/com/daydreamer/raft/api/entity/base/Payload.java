package com.daydreamer.raft.api.entity.base;

import com.daydreamer.raft.api.entity.constant.LogType;

import java.io.Serializable;

/**
 * @author Daydreamer
 */
public class Payload implements Serializable {
    
    private Object object;
    
    private LogType logType;
    
    public Payload() {
    }
    
    public Payload(Object object, LogType logType) {
        this.object = object;
        this.logType = logType;
    }
    
    public Object getObject() {
        return object;
    }
    
    public void setObject(Object object) {
        this.object = object;
    }
    
    public LogType getLogType() {
        return logType;
    }
    
    public void setLogType(LogType logType) {
        this.logType = logType;
    }
}
