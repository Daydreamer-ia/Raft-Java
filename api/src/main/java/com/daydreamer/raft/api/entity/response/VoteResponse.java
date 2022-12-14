package com.daydreamer.raft.api.entity.response;

import com.daydreamer.raft.api.entity.Response;

/**
 * @author Daydreamer
 *
 * response to vote
 */
public class VoteResponse extends Response {
    
    /**
     * whether to vote for client
     */
    private boolean isVoted;
    
    public VoteResponse(boolean isVoted) {
        this.isVoted = isVoted;
    }
    
    public boolean isVoted() {
        return isVoted;
    }
    
    public void setVoted(boolean voted) {
        isVoted = voted;
    }
    
    @Override
    public String toString() {
        return "VoteResponse{" + "isVoted=" + isVoted + '}';
    }
}
