package com.daydreamer.raft.transport.entity.request;

import com.daydreamer.raft.transport.entity.Request;

/**
 * @author Daydreamer
 *
 * request to vote
 */
public class VoteRequest extends Request {
    
    private int term;
    
    private long logId;
    
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
}
