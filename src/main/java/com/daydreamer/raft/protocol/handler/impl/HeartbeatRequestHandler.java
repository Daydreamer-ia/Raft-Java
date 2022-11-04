package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.transport.entity.request.HeartbeatRequest;
import com.daydreamer.raft.transport.entity.response.HeartbeatResponse;

/**
 * @author Daydreamer
 */
public class HeartbeatRequestHandler implements RequestHandler<HeartbeatRequest, HeartbeatResponse> {
    
    @Override
    public HeartbeatResponse handle(HeartbeatRequest request) {
        // TODO refresh leader active time
        return null;
    }
    
    @Override
    public Class<HeartbeatRequest> getSource() {
        return HeartbeatRequest.class;
    }
}
