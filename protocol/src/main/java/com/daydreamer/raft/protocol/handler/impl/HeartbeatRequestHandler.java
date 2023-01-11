package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.api.entity.request.HeartbeatRequest;
import com.daydreamer.raft.api.entity.response.HeartbeatResponse;

/**
 * @author Daydreamer
 */
@SPIImplement("heartbeatRequestHandler")
public class HeartbeatRequestHandler
        implements RequestHandler<HeartbeatRequest, HeartbeatResponse> {
    
    /**
     * raftServer
     */
    private AbstractRaftServer raftServer;
    
    /**
     * raftMemberManager
     */
    private RaftMemberManager raftMemberManager;
    
    @Override
    public HeartbeatResponse handle(HeartbeatRequest request) {
        int term = request.getTerm();
        long logId = request.getLogId();
        // if term lower
        if (term < raftMemberManager.getSelf().getTerm()) {
            return new HeartbeatResponse();
        }
        // if log id lower
        if (logId < raftMemberManager.getSelf().getLogId()) {
            return new HeartbeatResponse();
        }
        // let current node to be follower
        // refresh leader active time if normal cluster
        raftServer.refreshLeaderActive();
        // refresh term
        raftMemberManager.getSelf().setTerm(request.getTerm());
        return new HeartbeatResponse();
    }
    
    @Override
    public Class<HeartbeatRequest> getSource() {
        return HeartbeatRequest.class;
    }

    public void setAbstractRaftServer(AbstractRaftServer raftServer) {
        this.raftServer = raftServer;
    }

    public void setRaftMemberManager(RaftMemberManager raftMemberManager) {
        this.raftMemberManager = raftMemberManager;
    }
}
