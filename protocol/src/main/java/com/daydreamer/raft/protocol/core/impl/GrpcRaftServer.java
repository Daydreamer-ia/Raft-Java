package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.core.FollowerNotifier;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.transport.connection.Connection;
import com.daydreamer.raft.transport.connection.ResponseCallBack;
import com.daydreamer.raft.api.entity.Request;
import com.daydreamer.raft.api.entity.Response;
import com.daydreamer.raft.api.entity.request.VoteCommitRequest;
import com.daydreamer.raft.api.entity.request.VoteRequest;
import com.daydreamer.raft.api.entity.response.VoteCommitResponse;
import com.daydreamer.raft.api.entity.response.VoteResponse;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
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
    
    public GrpcRaftServer(String raftConfigPath, RaftMemberManager raftMemberManager,
            FollowerNotifier followerNotifier) {
        super(new RaftPropertiesReader(raftConfigPath), raftMemberManager, followerNotifier);
    }
    
    @Override
    public Member getSelf() {
        return raftMemberManager.getSelf();
    }
    
    @Override
    protected void doStartServer() {
        try {
            int port = raftConfig.getPort();
            server = ServerBuilder.forPort(port).addService(new GrpcRequestServerCore()).build().start();
            LOGGER.info("[GrpcRaftServer] - Server started, listening on port: " + port);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                LOGGER.info("[GrpcRaftServer] - shutting down gRPC server since JVM is shutting down...");
                this.close();
                LOGGER.info("[GrpcRaftServer] - server shut down");
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
        if (!batchRequestMembers(new VoteRequest(self.getTerm(), self.getLogId()), response -> {
            // nothing to do
            return ((VoteResponse) response).isVoted();
        })) {
            return false;
        }
        return batchRequestMembers(new VoteCommitRequest(self.getTerm(), self.getLogId()), response -> {
            // nothing to do
            return ((VoteCommitResponse) response).isAccepted();
        });
    }
    
    /**
     * send request to all members
     *
     * @param request request
     * @return whether success half of all
     */
    private boolean batchRequestMembers(Request request, Predicate<Response> predicate) throws Exception {
        List<Member> members = raftMemberManager.getAllMember();
        // begin to request
        AtomicInteger count = new AtomicInteger(1);
        CountDownLatch countDownLatch = new CountDownLatch(members.size());
        for (Member member : members) {
            try {
                Connection connection = member.getConnection();
                connection.request(request, new ResponseCallBack() {
                
                    @Override
                    public void onSuccess(Response response) {
                        if (predicate.test(response)) {
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
                    }
                });
            } catch (Exception e) {
                // nothing to do
                e.printStackTrace();
            }
        }
        countDownLatch.await(members.size() * 2000, TimeUnit.MICROSECONDS);
        if (request instanceof VoteRequest) {
            System.out.println("term: " + ((VoteRequest) request).getTerm() + " get count: " + count.get());
        } else if (request instanceof VoteCommitRequest) {
            System.out.println("term: " + ((VoteCommitRequest) request).getTerm() + " get commit: " + count.get());
        }
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
