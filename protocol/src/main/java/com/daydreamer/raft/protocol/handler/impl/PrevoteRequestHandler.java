package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.api.entity.request.PrevoteRequest;
import com.daydreamer.raft.api.entity.response.PrevoteResponse;
import com.daydreamer.raft.protocol.aware.RaftMemberManagerAware;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.handler.RequestHandler;

/**
 * @author Daydreamer
 */
public class PrevoteRequestHandler implements RequestHandler<PrevoteRequest, PrevoteResponse>, RaftMemberManagerAware {
    
    /**
     * raft members
     */
    private RaftMemberManager raftMemberManager;
    
    /**
     * if accepted
     */
    private static final PrevoteResponse ACCEPTED = new PrevoteResponse(true);
    
    /**
     * if reject
     */
    private static final PrevoteResponse REJECT = new PrevoteResponse(false);
    
    @Override
    public PrevoteResponse handle(PrevoteRequest request) {
        // if term current node larger, then disagree
        int clientTerm = request.getTerm();
        long clientLogIdx = request.getLogIndex();
        // if more log
        if (clientTerm >= raftMemberManager.getSelf().getTerm()
                && clientLogIdx >= raftMemberManager.getSelf().getLogId()) {
            return ACCEPTED;
        }
        // or reject
        return REJECT;
    }
    
    @Override
    public Class<PrevoteRequest> getSource() {
        return PrevoteRequest.class;
    }
    
    @Override
    public void setRaftMemberManager(RaftMemberManager raftMemberManager) {
        this.raftMemberManager = raftMemberManager;
    }
}
