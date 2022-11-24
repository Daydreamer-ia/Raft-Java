package com.daydreamer.raft.api.entity.base;

import com.daydreamer.raft.api.entity.Response;

/**
 * @author Daydreamer
 */
public abstract class CommittedResponse extends Response {
    
    private boolean accepted;
    
    public CommittedResponse(boolean accepted) {
        this.accepted = accepted;
    }
    
    public boolean isAccepted() {
        return accepted;
    }
    
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
