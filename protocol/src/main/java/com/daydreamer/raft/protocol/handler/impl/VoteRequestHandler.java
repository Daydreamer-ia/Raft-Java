package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.protocol.aware.ReplicatedStateMachineAware;
import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.protocol.aware.RaftMemberManagerAware;
import com.daydreamer.raft.protocol.aware.RaftServerAware;
import com.daydreamer.raft.api.entity.request.VoteRequest;
import com.daydreamer.raft.api.entity.response.VoteResponse;
import com.daydreamer.raft.protocol.storage.ReplicatedStateMachine;

/**
 * @author Daydreamer
 *
 * vote request handler
 */
@SuppressWarnings("all")
public class VoteRequestHandler implements RequestHandler<VoteRequest, VoteResponse>,
        RaftMemberManagerAware, RaftServerAware, ReplicatedStateMachineAware {
    
    /**
     * if reject
     */
    private static final VoteResponse REJECT = new VoteResponse(false);
    
    /**
     * if accept
     */
    private static final VoteResponse ACCEPTED = new VoteResponse(true);
    
    /**
     * raftMemberManager
     */
    private RaftMemberManager raftMemberManager;
    
    /**
     * raftServer
     */
    private AbstractRaftServer raftServer;
    
    /**
     * log storage
     */
    private ReplicatedStateMachine replicatedStateMachine;
    
    @Override
    public synchronized VoteResponse handle(VoteRequest request) {
        // reject vote if leader existed
        if (raftServer.leaderExisted()) {
            return REJECT;
        }
        // if current node has voted this term, then reject
        if (request.getTerm() <= raftServer.getLastTermCurrentNodeHasVoted()) {
            return REJECT;
        }
        // determine whether to vote base on lastVotedTerm
        int term = request.getTerm();
        long logIndex = request.getLogIndex();
        // wait log committed finish
        synchronized (replicatedStateMachine) {
            // lower term
            if (term < raftMemberManager.getSelf().getLogId()) {
                return REJECT;
            }
            // update last term
            raftServer.refreshLastVotedTerm(request.getTerm());
            // lower log id
            if (logIndex < raftMemberManager.getSelf().getLogId()) {
                return REJECT;
            }
            // refresh may be leader active time
            raftServer.refreshLeaderActive();
            return ACCEPTED;
        }
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
    
    @Override
    public void setReplicatedStateMachine(ReplicatedStateMachine replicatedStateMachine) {
        this.replicatedStateMachine = replicatedStateMachine;
    }
}
