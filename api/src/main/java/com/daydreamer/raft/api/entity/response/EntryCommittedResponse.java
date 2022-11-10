package com.daydreamer.raft.api.entity.response;

import com.daydreamer.raft.api.entity.Response;

/**
 * @author Daydreamer
 */
public class EntryCommittedResponse extends Response {

    private boolean accepted;
    
    public EntryCommittedResponse(boolean accepted) {
        this.accepted = accepted;
    }
    
    public boolean isAccepted() {
        return accepted;
    }
    
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
