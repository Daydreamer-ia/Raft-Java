package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.api.entity.request.PrevoteRequest;
import com.daydreamer.raft.api.entity.response.PrevoteResponse;
import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.common.loader.GroupAware;
import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.api.entity.request.VoteCommitRequest;
import com.daydreamer.raft.api.entity.request.VoteRequest;
import com.daydreamer.raft.api.entity.response.VoteCommitResponse;
import com.daydreamer.raft.api.entity.response.VoteResponse;
import com.daydreamer.raft.protocol.handler.RequestHandlerHolder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daydreamer
 */
@SPIImplement("abstractRaftServer")
public class GrpcRaftServer extends AbstractRaftServer implements GroupAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcRaftServer.class);

    /**
     * server
     */
    private Server server;

    private String groupKey;

    public GrpcRaftServer() {
    }

    @Override
    public Member getSelf() {
        return raftMemberManager.getSelf();
    }

    @Override
    protected void doStartServer() {
        try {
            int port = raftMemberManager.getSelf().getPort();
            server = ServerBuilder.forPort(port).addService(new GrpcRequestServerCore(new RequestHandlerHolder(groupKey))).build()
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
        if (!logSender.batchRequestMembers(new VoteRequest(self.getTerm(), self.getLogId()), raftMemberManager.getAllMember(), response -> {
            // nothing to do
            return ((VoteResponse) response).isVoted();
        })) {
            return false;
        }
        return logSender
                .batchRequestMembers(new VoteCommitRequest(self.getTerm(), self.getLogId()), raftMemberManager.getAllMember(), response -> {
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
        return logSender.batchRequestMembers(prevoteRequest, raftMemberManager.getAllMember(), (response) -> {
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
        } catch (Exception e) {
            throw new IllegalStateException(
                    "[GrpcRaftServer] - Fail to close server, because " + e.getLocalizedMessage());
        }
    }

    @Override
    public void setGroupKey(String key) {
        this.groupKey = key;
    }
}
