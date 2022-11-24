package com.daydreamer.raft.api.entity.base;

import com.daydreamer.raft.api.entity.constant.LogType;

import java.io.Serializable;

/**
 * @author Daydreamer
 */
public class Payload<T> implements Serializable {
    
    private T object;
    
    private LogType logType;
    
    public Payload() {
    }
    
    public Payload(T object, LogType logType) {
        this.object = object;
        this.logType = logType;
    }
    
    public T getObject() {
        return object;
    }
    
    public void setObject(T object) {
        this.object = object;
    }
    
    public LogType getLogType() {
        return logType;
    }
    
    public void setLogType(LogType logType) {
        this.logType = logType;
    }
}
