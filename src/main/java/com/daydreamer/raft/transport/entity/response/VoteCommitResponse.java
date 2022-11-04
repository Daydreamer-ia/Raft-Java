package com.daydreamer.raft.transport.entity.response;

import com.daydreamer.raft.transport.entity.Response;

/**
 * @author Daydreamer
 */
public class VoteCommitResponse extends Response {
    
    private boolean accepted;
    
    public VoteCommitResponse(boolean accepted) {
        this.accepted = accepted;
    }
    
    public boolean isAccepted() {
        return accepted;
    }
    
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
