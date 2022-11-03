package com.daydreamer.raft.transport.entity.request;

import com.daydreamer.raft.transport.entity.Request;

/**
 * @author Daydreamer
 *
 * request to vote
 */
public class VoteRequest extends Request {
    
    private int lastTerm;
    
    private long lastLogIndex;
    
    public int getLastTerm() {
        return lastTerm;
    }
    
    public void setLastTerm(int lastTerm) {
        this.lastTerm = lastTerm;
    }
    
    public long getLastLogIndex() {
        return lastLogIndex;
    }
    
    public void setLastLogIndex(long lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
    }
}
