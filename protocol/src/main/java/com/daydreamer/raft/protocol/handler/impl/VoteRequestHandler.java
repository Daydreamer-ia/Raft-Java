package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.api.entity.request.VoteRequest;
import com.daydreamer.raft.api.entity.response.VoteResponse;
import com.daydreamer.raft.protocol.storage.ReplicatedStateMachine;

/**
 * @author Daydreamer
 * <p>
 * vote request handler
 */
@SPIImplement("voteRequestHandler")
@SuppressWarnings("all")
public class VoteRequestHandler implements RequestHandler<VoteRequest, VoteResponse> {

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
    private AbstractRaftServer abstractRaftServer;

    /**
     * log storage
     */
    private ReplicatedStateMachine replicatedStateMachine;

    @Override
    public synchronized VoteResponse handle(VoteRequest request) {
        // reject vote if leader existed
        if (abstractRaftServer.leaderExisted()) {
            return REJECT;
        }
        // if current node has voted this term, then reject
        if (request.getTerm() <= abstractRaftServer.getLastTermCurrentNodeHasVoted()) {
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
            abstractRaftServer.refreshLastVotedTerm(request.getTerm());
            // lower log id
            if (logIndex < raftMemberManager.getSelf().getLogId()) {
                return REJECT;
            }
            // refresh may be leader active time
            abstractRaftServer.refreshLeaderActive();
            return ACCEPTED;
        }
    }

    @Override
    public Class<VoteRequest> getSource() {
        return VoteRequest.class;
    }

    public void setRaftMemberManager(RaftMemberManager raftMemberManager) {
        this.raftMemberManager = raftMemberManager;
    }

    public void setAbstractRaftServer(AbstractRaftServer abstractRaftServer) {
        this.abstractRaftServer = abstractRaftServer;
    }

    public void setReplicatedStateMachine(ReplicatedStateMachine replicatedStateMachine) {
        this.replicatedStateMachine = replicatedStateMachine;
    }
}
