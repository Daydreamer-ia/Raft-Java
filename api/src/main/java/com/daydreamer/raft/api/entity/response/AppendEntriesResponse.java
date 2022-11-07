package com.daydreamer.raft.api.entity.response;

import com.daydreamer.raft.api.entity.Response;

/**
 * @author Daydreamer
 */
public class AppendEntriesResponse extends Response {
    
    /**
     * whether accepted
     */
    private boolean accepted;
    
    public AppendEntriesResponse(boolean accepted) {
        this.accepted = accepted;
    }
    
    public boolean isAccepted() {
        return accepted;
    }
    
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
