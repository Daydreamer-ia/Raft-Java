package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.api.entity.request.MemberChangeRequest;
import com.daydreamer.raft.api.entity.response.MemberChangeResponse;
import com.daydreamer.raft.protocol.handler.RequestHandler;

/**
 * @author Daydreamer
 */
public class MemberChangeRequestHandler implements RequestHandler<MemberChangeRequest, MemberChangeResponse> {
    
    @Override
    public MemberChangeResponse handle(MemberChangeRequest request) {
        return null;
    }
    
    @Override
    public Class<MemberChangeRequest> getSource() {
        return MemberChangeRequest.class;
    }
}
