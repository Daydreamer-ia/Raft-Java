package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.transport.entity.request.VoteCommitRequest;
import com.daydreamer.raft.transport.entity.response.VoteCommitResponse;

/**
 * @author Daydreamer
 */
public class VoteCommitRequestHandler implements RequestHandler<VoteCommitRequest, VoteCommitResponse> {
    
    @Override
    public VoteCommitResponse handle(VoteCommitRequest request) {
        // TODO to be follower
        return null;
    }
    
    @Override
    public Class<VoteCommitRequest> getSource() {
        return VoteCommitRequest.class;
    }
}
