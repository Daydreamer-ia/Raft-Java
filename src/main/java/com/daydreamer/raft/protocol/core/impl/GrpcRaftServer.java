package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.core.FollowerNotifier;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.protocol.entity.RaftConfig;
import com.daydreamer.raft.transport.connection.Connection;
import com.daydreamer.raft.transport.connection.ResponseCallBack;
import com.daydreamer.raft.transport.connection.impl.grpc.GrpcRequestServerCore;
import com.daydreamer.raft.transport.entity.Request;
import com.daydreamer.raft.transport.entity.Response;
import com.daydreamer.raft.transport.entity.request.VoteCommitRequest;
import com.daydreamer.raft.transport.entity.request.VoteRequest;
import com.daydreamer.raft.transport.entity.response.VoteResponse;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Daydreamer
 */
public class GrpcRaftServer extends AbstractRaftServer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcRaftServer.class);
    
    /**
     * server
     */
    private Server server;
    
    /**
     * raft member manager
     */
    private RaftMemberManager raftMemberManager;
    
    /**
     * notify followers if current node is leader
     */
    private FollowerNotifier followerNotifier;
    
    public GrpcRaftServer(RaftConfig raftConfig, RaftMemberManager raftMemberManager,
            FollowerNotifier followerNotifier) {
        super(raftConfig);
        this.raftMemberManager = raftMemberManager;
        this.followerNotifier = followerNotifier;
    }
    
    @Override
    protected Member getSelf() {
        return raftMemberManager.getSelf();
    }
    
    @Override
    protected void doStartServer() {
        try {
            int port = raftConfig.getPort();
            server = ServerBuilder.forPort(port).addService(new GrpcRequestServerCore()).build().start();
            LOGGER.trace("[GrpcRaftServer] - Server started, listening on port: " + port);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                LOGGER.trace("[GrpcRaftServer] - shutting down gRPC server since JVM is shutting down...");
                this.close();
                LOGGER.trace("[GrpcRaftServer] - server shut down");
            }));
            // init
            followerNotifier.init();
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
        // if success half of all
        // then commit
        if (!batchRequestMembers(new VoteRequest(self.getTerm(), self.getLogId()))) {
            return false;
        }
        return batchRequestMembers(new VoteCommitRequest(self.getTerm(), self.getLogId()));
    }
    
    /**
     * send request to all members
     *
     * @param request request
     * @return whether success half of all
     */
    private boolean batchRequestMembers(Request request) throws Exception {
        List<Member> members = raftMemberManager.getAllMember();
        // begin to request
        AtomicInteger count = new AtomicInteger(1);
        CountDownLatch countDownLatch = new CountDownLatch(members.size());
        for (Member member : members) {
            try {
                Connection connection = member.getConnection();
                connection.request(request, 3000L, new ResponseCallBack() {
                
                    @Override
                    public void onSuccess(Response response) {
                        if (response instanceof VoteResponse) {
                            count.incrementAndGet();
                        }
                        countDownLatch.countDown();
                    }
                
                    @Override
                    public void onFail(Exception e) {
                        // nothing to do
                        countDownLatch.countDown();
                    }
                
                    @Override
                    public void onTimeout() {
                        // nothing to do
                        countDownLatch.countDown();
                    }
                });
            } catch (Exception e) {
                // nothing to do
            }
        }
        countDownLatch.await(3000, TimeUnit.MICROSECONDS);
        return count.get() > (members.size() + 1) / 2;
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
