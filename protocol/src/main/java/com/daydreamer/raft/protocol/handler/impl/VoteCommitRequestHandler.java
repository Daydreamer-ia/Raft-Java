package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.protocol.aware.RaftServerAware;
import com.daydreamer.raft.api.entity.request.VoteCommitRequest;
import com.daydreamer.raft.api.entity.response.VoteCommitResponse;

/**
 * @author Daydreamer
 */
public class VoteCommitRequestHandler implements RequestHandler<VoteCommitRequest, VoteCommitResponse>,
        RaftServerAware {
    
    /**
     * raftServer
     */
    private AbstractRaftServer raftServer;
    
    @Override
    public VoteCommitResponse handle(VoteCommitRequest request) {
        // judge the request whether from last term current node has voted
        if (request.getTerm() == raftServer.getLastTermCurrentNodeHasVoted()) {
            // refresh leader active time
            raftServer.refreshLeaderActive();
            return new VoteCommitResponse(true);
        }
        // reject if current node has largest term and log id
        if (request.getTerm() < raftServer.getSelf().getTerm()) {
            return new VoteCommitResponse(false);
        }
        if (request.getLogId() < raftServer.getSelf().getLogId()) {
            return new VoteCommitResponse(false);
        }
        // refresh leader active time
        raftServer.refreshLeaderActive();
        return new VoteCommitResponse(true);
    }
    
    @Override
    public Class<VoteCommitRequest> getSource() {
        return VoteCommitRequest.class;
    }
    
    @Override
    public void setRaftServer(AbstractRaftServer raftServer) {
        this.raftServer = raftServer;
    }
}
