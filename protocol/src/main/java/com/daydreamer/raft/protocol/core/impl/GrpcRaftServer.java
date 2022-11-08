package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.core.AbstractFollowerNotifier;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.protocol.entity.RaftConfig;
import com.daydreamer.raft.protocol.storage.StorageRepository;
import com.daydreamer.raft.api.entity.request.VoteCommitRequest;
import com.daydreamer.raft.api.entity.request.VoteRequest;
import com.daydreamer.raft.api.entity.response.VoteCommitResponse;
import com.daydreamer.raft.api.entity.response.VoteResponse;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author Daydreamer
 */
public class GrpcRaftServer extends AbstractRaftServer {
    
    private static final Logger LOGGER = Logger.getLogger(GrpcRaftServer.class.getSimpleName());
    
    /**
     * server
     */
    private Server server;
    
    private RaftMemberManager raftMemberManager;
    
    public GrpcRaftServer(RaftConfig raftConfigPath, RaftMemberManager raftMemberManager,
            AbstractFollowerNotifier abstractFollowerNotifier, StorageRepository storageRepository) {
        super(raftConfigPath, raftMemberManager, abstractFollowerNotifier, storageRepository);
        this.raftMemberManager = raftMemberManager;
    }
    
    @Override
    public Member getSelf() {
        return raftMemberManager.getSelf();
    }
    
    @Override
    protected void doStartServer() {
        try {
            int port = raftConfig.getPort();
            server = ServerBuilder.forPort(port).addService(new GrpcRequestServerCore(requestHandlerHolder)).build().start();
            LOGGER.info("[GrpcRaftServer] - Server started, listening on port: " + port);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                LOGGER.info("[GrpcRaftServer] - shutting down gRPC server since JVM is shutting down...");
                this.close();
                LOGGER.info("[GrpcRaftServer] - server shut down");
            }));
        } catch (Exception e) {
            throw new IllegalStateException(
                    "[GrpcRaftServer] - Fail to init server, because " + e.getLocalizedMessage());
        }
    }
    
    @Override
    public boolean requestVote() throws Exception {
        // request vote
        Member self = raftMemberManager.getSelf();
        self.increaseTerm();
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
        return raftMemberManager.batchRequestMembers(new VoteCommitRequest(self.getTerm(), self.getLogId()), response -> {
            // nothing to do
            return ((VoteCommitResponse) response).isAccepted();
        });
    }
    
    @Override
    public boolean isLeader() {
        return raftMemberManager.isLeader();
    }
    
    @Override
    public void close() {
        try {
            if (server != null) {
                server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "[GrpcRaftServer] - Fail to close server, because " + e.getLocalizedMessage());
        }
    }
}
