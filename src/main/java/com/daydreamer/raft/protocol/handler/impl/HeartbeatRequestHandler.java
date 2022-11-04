package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.protocol.aware.RaftMemberManagerAware;
import com.daydreamer.raft.protocol.aware.RaftServerAware;
import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.transport.entity.request.HeartbeatRequest;
import com.daydreamer.raft.transport.entity.response.HeartbeatResponse;

/**
 * @author Daydreamer
 */
public class HeartbeatRequestHandler
        implements RequestHandler<HeartbeatRequest, HeartbeatResponse>, RaftServerAware, RaftMemberManagerAware {
    
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
        return new HeartbeatResponse();
    }
    
    @Override
    public Class<HeartbeatRequest> getSource() {
        return HeartbeatRequest.class;
    }
    
    @Override
    public void setRaftServer(AbstractRaftServer raftServer) {
        this.raftServer = raftServer;
    }
    
    @Override
    public void setRaftMemberManager(RaftMemberManager raftMemberManager) {
        this.raftMemberManager = raftMemberManager;
    }
}
