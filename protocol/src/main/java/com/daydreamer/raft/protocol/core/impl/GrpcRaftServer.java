package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.api.entity.request.PrevoteRequest;
import com.daydreamer.raft.api.entity.response.PrevoteResponse;
import com.daydreamer.raft.common.service.PropertiesReader;
import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.core.AbstractFollowerNotifier;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.protocol.entity.RaftConfig;
import com.daydreamer.raft.protocol.storage.ReplicatedStateMachine;
import com.daydreamer.raft.api.entity.request.VoteCommitRequest;
import com.daydreamer.raft.api.entity.request.VoteRequest;
import com.daydreamer.raft.api.entity.response.VoteCommitResponse;
import com.daydreamer.raft.api.entity.response.VoteResponse;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * @author Daydreamer
 */
public class GrpcRaftServer extends AbstractRaftServer {
    
    private static final Logger LOGGER = Logger.getLogger(GrpcRaftServer.class);
    
    /**
     * server
     */
    private Server server;
    
    /**
     * member manager
     */
    private RaftMemberManager raftMemberManager;
    
    /**
     * property reader
     */
    private PropertiesReader<RaftConfig> raftPropertiesReader;
    
    public GrpcRaftServer(PropertiesReader<RaftConfig> raftPropertiesReader, RaftMemberManager raftMemberManager,
            AbstractFollowerNotifier abstractFollowerNotifier, ReplicatedStateMachine replicatedStateMachine) {
        super(raftPropertiesReader.getProperties(), raftMemberManager, abstractFollowerNotifier, replicatedStateMachine);
        this.raftMemberManager = raftMemberManager;
        this.raftPropertiesReader = raftPropertiesReader;
    }
    
    @Override
    public Member getSelf() {
        return raftMemberManager.getSelf();
    }
    
    @Override
    protected void doStartServer() {
        try {
            int port = raftConfig.getPort();
            server = ServerBuilder.forPort(port).addService(new GrpcRequestServerCore(requestHandlerHolder)).build()
                    .start();
            LOGGER.info("Server started, listening on port: " + port);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "[GrpcRaftServer] - Fail to init server, because " + e.getLocalizedMessage());
        }
    }
    
    @Override
    public boolean requestVote() throws Exception {
        // request vote
        Member self = raftMemberManager.getSelf();
        // refresh candidate
        refreshCandidateActive();
        // if success half of all
        // then commit
        if (!raftMemberManager.batchRequestMembers(new VoteRequest(self.getTerm(), self.getLogId()), response -> {
            // nothing to do
            return ((VoteResponse) response).isVoted();
        })) {
            return false;
        }
        return raftMemberManager
                .batchRequestMembers(new VoteCommitRequest(self.getTerm(), self.getLogId()), response -> {
                    // nothing to do
                    return ((VoteCommitResponse) response).isAccepted();
                });
    }
    
    @Override
    protected boolean prevote() throws Exception {
        // refresh candidate
        refreshCandidateActive();
        Member self = raftMemberManager.getSelf();
        PrevoteRequest prevoteRequest = new PrevoteRequest(self.getTerm(), self.getLogId());
        return raftMemberManager.batchRequestMembers(prevoteRequest, (response) -> {
            if (response instanceof PrevoteResponse) {
                return ((PrevoteResponse) response).isAgree();
            }
            return false;
        });
    }
    
    @Override
    public boolean isLeader() {
        return raftMemberManager.isLeader();
    }
    
    @Override
    public void close() {
        try {
            // close super
            super.close();
            // close sub
            if (server != null) {
                server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            }
            if (raftPropertiesReader != null) {
                raftPropertiesReader.close();
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "[GrpcRaftServer] - Fail to close server, because " + e.getLocalizedMessage());
        }
    }
}
