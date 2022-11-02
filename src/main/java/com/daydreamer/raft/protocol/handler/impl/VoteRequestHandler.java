package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.transport.entity.request.VoteRequest;
import com.daydreamer.raft.transport.entity.response.VoteResponse;

/**
 * @author Daydreamer
 *
 * vote request handler
 */
public class VoteRequestHandler implements RequestHandler<VoteRequest, VoteResponse> {
    
    @Override
    public VoteResponse handle(VoteRequest request) {
        return new VoteResponse(false);
    }
    
    @Override
    public Class<VoteRequest> getSource() {
        return VoteRequest.class;
    }
}
