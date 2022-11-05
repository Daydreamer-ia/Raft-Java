package com.daydreamer.raft.api.entity.request;

import com.daydreamer.raft.api.entity.Request;

/**
 * @author Daydreamer
 *
 * heart beat
 */
public class HeartbeatRequest extends Request {
    
    private int term;
    
    private long logId;
    
    public HeartbeatRequest(int term, long logId) {
        this.term = term;
        this.logId = logId;
    }
    
    public long getLogId() {
        return logId;
    }
    
    public void setLogId(long logId) {
        this.logId = logId;
    }
    
    public int getTerm() {
        return term;
    }
    
    public void setTerm(int term) {
        this.term = term;
    }
}
