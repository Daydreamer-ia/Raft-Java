package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.protocol.aware.StorageRepositoryAware;
import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.protocol.aware.RaftMemberManagerAware;
import com.daydreamer.raft.protocol.aware.RaftServerAware;
import com.daydreamer.raft.api.entity.request.VoteRequest;
import com.daydreamer.raft.api.entity.response.VoteResponse;
import com.daydreamer.raft.protocol.storage.StorageRepository;

/**
 * @author Daydreamer
 *
 * vote request handler
 */
@SuppressWarnings("all")
public class VoteRequestHandler implements RequestHandler<VoteRequest, VoteResponse>,
        RaftMemberManagerAware, RaftServerAware, StorageRepositoryAware {
    
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
    private StorageRepository storageRepository;
    
    @Override
    public synchronized VoteResponse handle(VoteRequest request) {
        // if current node has voted this term, then reject
        if (request.getTerm() <= raftServer.getLastTermCurrentNodeHasVoted()) {
            return new VoteResponse(false);
        }
        // determine whether to vote base on lastVotedTerm
        int term = request.getTerm();
        long logIndex = request.getLogIndex();
        // wait log committed finish
        synchronized (storageRepository) {
            // lower term
            if (term < raftMemberManager.getSelf().getLogId()) {
                return new VoteResponse(false);
            }
            // update last term
            raftServer.getSelf().setTerm(request.getTerm());
            raftServer.refreshLastVotedTerm(request.getTerm());
            // lower log id
            if (logIndex < raftMemberManager.getSelf().getLogId()) {
                return new VoteResponse(false);
            }
            // refresh may be leader active time
            raftServer.refreshLeaderActive();
            return new VoteResponse(true);
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
    public void setStorageRepository(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }
}
