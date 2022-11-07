package com.daydreamer.raft.api.entity.request;

import com.daydreamer.raft.api.entity.Request;

/**
 * @author Daydreamer
 */
public class EntryCommittedRequest extends Request {
    
    private long logId;
    
    private int term;
    
    public EntryCommittedRequest(long logId, int term) {
        this.logId = logId;
        this.term = term;
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
