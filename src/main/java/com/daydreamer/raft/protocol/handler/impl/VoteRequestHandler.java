package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.transport.entity.request.VoteRequest;
import com.daydreamer.raft.transport.entity.response.VoteResponse;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Daydreamer
 *
 * vote request handler
 */
public class VoteRequestHandler implements RequestHandler<VoteRequest, VoteResponse> {
    
    /**
     * the latest request term is
     */
    private volatile int lastVotedTerm = 0;
    
    @Override
    public synchronized VoteResponse handle(VoteRequest request) {
        // TODO determine whether to vote base on lastVotedTerm
        return new VoteResponse(false);
    }
    
    @Override
    public Class<VoteRequest> getSource() {
        return VoteRequest.class;
    }
}
