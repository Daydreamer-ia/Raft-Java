package com.daydreamer.raft.transport.entity.request;

import com.daydreamer.raft.transport.entity.Request;

/**
 * @author Daydreamer
 *
 * request to vote
 */
public class VoteRequest extends Request {
    
    private int term;
    
    private long logIndex;
    
    public VoteRequest() {
    }
    
    public VoteRequest(int term, long logIndex) {
        this.term = term;
        this.logIndex = logIndex;
    }
    
    public int getTerm() {
        return term;
    }
    
    public void setTerm(int term) {
        this.term = term;
    }
    
    public long getLogIndex() {
        return logIndex;
    }
    
    public void setLogIndex(long logIndex) {
        this.logIndex = logIndex;
    }
}
