package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.protocol.aware.RaftMemberManagerAware;
import com.daydreamer.raft.protocol.aware.RaftServerAware;
import com.daydreamer.raft.api.entity.request.VoteRequest;
import com.daydreamer.raft.api.entity.response.VoteResponse;

/**
 * @author Daydreamer
 *
 * vote request handler
 */
public class VoteRequestHandler implements RequestHandler<VoteRequest, VoteResponse>,
        RaftMemberManagerAware, RaftServerAware {
    
    /**
     * raftMemberManager
     */
    private RaftMemberManager raftMemberManager;
    
    /**
     * raftServer
     */
    private AbstractRaftServer raftServer;
    
    @Override
    public synchronized VoteResponse handle(VoteRequest request) {
        // if current node has voted this term, then reject
        if (request.getTerm() <= raftServer.getLastTermCurrentNodeHasVoted()) {
            return new VoteResponse(false);
        }
        // determine whether to vote base on lastVotedTerm
        int term = request.getTerm();
        long logIndex = request.getLogIndex();
        // lower term
        if (term < raftMemberManager.getSelf().getLogId()) {
            return new VoteResponse(false);
        }
        // lower log id
        if (logIndex < raftMemberManager.getSelf().getLogId()) {
            return new VoteResponse(false);
        }
        // refresh candidate active time
        raftServer.refreshCandidateActive();
        raftServer.refreshLastVotedTerm(request.getTerm());
        return new VoteResponse(true);
    }
    
    @Override
    public Class<VoteRequest> getSource() {
        return VoteRequest.class;
    }
    
    @Override
    public void setRaftMemberManager(RaftMemberManager raftMemberManager) {
        this.raftMemberManager = raftMemberManager;
    }
    
    @Override
    public void setRaftServer(AbstractRaftServer raftServer) {
        this.raftServer = raftServer;
    }
}
