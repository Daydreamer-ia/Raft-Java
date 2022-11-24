package com.daydreamer.raft.api.entity.response;

import com.daydreamer.raft.api.entity.base.CommittedResponse;

/**
 * @author Daydreamer
 */
public class VoteCommitResponse extends CommittedResponse {
    
    public VoteCommitResponse(boolean accepted) {
        super(accepted);
    }
}
