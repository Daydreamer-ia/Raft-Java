package com.daydreamer.raft.transport.entity.request;

import com.daydreamer.raft.transport.entity.Request;

/**
 * @author Daydreamer
 *
 * heart beat
 */
public class HeartbeatRequest extends Request {
    
    private long term;
    
    private long logId;
    
    public HeartbeatRequest(long term, long logId) {
        this.term = term;
        this.logId = logId;
    }
    
    public long getLogId() {
        return logId;
    }
    
    public void setLogId(long logId) {
        this.logId = logId;
    }
    
    public long getTerm() {
        return term;
    }
    
    public void setTerm(long term) {
        this.term = term;
    }
}
