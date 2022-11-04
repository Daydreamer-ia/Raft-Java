package com.daydreamer.raft.transport.entity.request;

import com.daydreamer.raft.transport.entity.Request;

/**
 * @author Daydreamer
 *
 * if success to get half of all, then commit and tell follower
 */
public class VoteCommitRequest extends Request {
    
    private int term;
    
    private long logId;
    
    public VoteCommitRequest() {
    }
    
    public VoteCommitRequest(int term, long logId) {
        this.term = term;
        this.logId = logId;
    }
    
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
