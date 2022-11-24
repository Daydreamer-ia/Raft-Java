package com.daydreamer.raft.api.entity.response;

import com.daydreamer.raft.api.entity.base.CommittedResponse;

/**
 * @author Daydreamer
 */
public class MemberChangeCommitRequest extends CommittedResponse {
    
    public MemberChangeCommitRequest(boolean accepted) {
        super(accepted);
    }
}
