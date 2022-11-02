package com.daydreamer.raft.transport.entity.response;

import com.daydreamer.raft.transport.entity.Response;

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
    
    @Override
    public String toString() {
        return "VoteResponse{" + "isVoted=" + isVoted + '}';
    }
}
