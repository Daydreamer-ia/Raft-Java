package com.daydreamer.raft.api.entity.request;

import com.daydreamer.raft.api.entity.Request;

/**
 * @author Daydreamer
 */
public class PrevoteRequest extends Request {
    
    /**
     * term from client
     */
    private int term;
    
    /**
     * log index
     */
    private long logIndex;
    
    public PrevoteRequest() {
    }
    
    public PrevoteRequest(int term, long logIndex) {
        this.term = term;
        this.logIndex = logIndex;
    }
    
    public long getLogIndex() {
        return logIndex;
    }
    
    public void setLogIndex(long logIndex) {
        this.logIndex = logIndex;
    }
    
    public int getTerm() {
        return term;
    }
    
    public void setTerm(int term) {
        this.term = term;
    }
}
