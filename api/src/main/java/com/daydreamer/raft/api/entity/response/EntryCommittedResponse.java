package com.daydreamer.raft.api.entity.response;

import com.daydreamer.raft.api.entity.base.CommittedResponse;

/**
 * @author Daydreamer
 */
public class EntryCommittedResponse extends CommittedResponse {
    
    public EntryCommittedResponse(boolean accepted) {
        super(accepted);
    }
}
