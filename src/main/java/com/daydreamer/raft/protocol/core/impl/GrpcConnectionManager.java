package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.common.entity.SimpleFuture;
import com.daydreamer.raft.common.utils.MsgUtils;
import com.daydreamer.raft.protocol.constant.NodeStatus;
import com.daydreamer.raft.protocol.core.ConnectionManager;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.protocol.entity.RaftConfig;
import com.daydreamer.raft.transport.connection.Connection;
import com.daydreamer.raft.transport.connection.ResponseCallBack;
import com.daydreamer.raft.transport.connection.impl.grpc.GrpcConnection;
import com.daydreamer.raft.transport.entity.Response;
import com.daydreamer.raft.transport.entity.request.HeartbeatRequest;
import com.daydreamer.raft.transport.entity.request.SetupRequest;
import com.daydreamer.raft.transport.entity.response.SetupResponse;
import com.daydreamer.raft.transport.grpc.Message;
import com.daydreamer.raft.transport.grpc.RequesterGrpc;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Daydreamer
 * <p>
 * It is a implmement to retain grpc connection.
 */
public class GrpcConnectionManager implements ConnectionManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcConnectionManager.class);
    
    private RaftMemberManager raftMemberManager;
    
    private RaftConfig raftConfig;
    
    public GrpcConnectionManager(RaftMemberManager raftMemberManager, RaftConfig raftConfig) {
        this.raftMemberManager = raftMemberManager;
        this.raftConfig = raftConfig;
    }
    
    /**
     * executor for schedule
     */
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, r -> {
        Thread thread = new Thread(r);
        thread.setName("refresh-active-time-thread");
        thread.setDaemon(true);
        return thread;
    });
    
    @Override
    public void init() {
        Runnable job = () -> {
            try {
                List<Member> allMember = raftMemberManager.getAllMember();
                // not active a period time
                CountDownLatch countDownLatch = new CountDownLatch(allMember.size());
                List<Member> unconnected = new ArrayList<>(allMember.size());
                allMember.forEach(member -> {
                    try {
                        long lastActiveTime = member.getLastActiveTime();
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastActiveTime > raftConfig.getHeartInterval()) {
                            Connection connection = member.getConnection();
                            if (connection != null) {
                                connection.request(new HeartbeatRequest(), 2000, new ResponseCallBack() {
                                    
                                    @Override
                                    public void onSuccess(Response response) {
                                        member.setStatus(NodeStatus.UP);
                                        refreshActiveTime(member.getMemberId());
                                        countDownLatch.countDown();
                                    }
                                    
                                    @Override
                                    public void onFail(Exception e) {
                                        // remove connection
                                        member.setStatus(NodeStatus.DOWN);
                                        countDownLatch.countDown();
                                        // try connect
                                        unconnected.add(member);
                                    }
    
                                    @Override
                                    public void onTimeout() {
                                        countDownLatch.countDown();
                                    }
                                });
                            } else {
                                // try to connect
                                unconnected.add(member);
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("[GrpcConnectionManager] - Schedule error when check connection, because " + e
                                .getLocalizedMessage() + ". current member" + member.getAddress());
                    }
                });
                // help gc
                allMember = null;
                // wait for response
                countDownLatch.await(2000L, TimeUnit.MILLISECONDS);
                // try to reconnect
                reconnect(unconnected);
            } catch (Exception e) {
                // nothing to do
                e.printStackTrace();
            }
        };
        int random = new Random().nextInt(raftConfig.getHeartInterval() / 2);
        // submit job
        executor.scheduleAtFixedRate(job, raftConfig.getHeartInterval() + random, random, TimeUnit.MICROSECONDS);
    }
    
    /**
     * connect to member
     *
     * @param requesterBlockingStub stub
     */
    public String connectToMember(RequesterGrpc.RequesterBlockingStub requesterBlockingStub) {
        Message msg = MsgUtils.convertMsg(new SetupRequest());
        Response response = MsgUtils.convertResponse(requesterBlockingStub.request(msg));
        if (response instanceof SetupResponse) {
            return ((SetupResponse) response).getMemberId();
        }
        return null;
    }
    
    /**
     * handle unconnected member
     *
     * @param members members
     */
    private void reconnect(List<Member> members) {
        List<Future<Connection>> connections = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(connections.size());
        members.forEach(member -> {
            SimpleFuture<Connection> future = new SimpleFuture<>(() -> {
                // close old conn
                Connection oldConn = member.getConnection();
                if (oldConn != null) {
                    oldConn.close();
                }
                GrpcConnection connection = null;
                ManagedChannel channel = null;
                try {
                    channel = ManagedChannelBuilder.forAddress(member.getIp(), member.getPort()).usePlaintext()
                            .build();
                    RequesterGrpc.RequesterBlockingStub blockingStub = RequesterGrpc.newBlockingStub(channel);
                    String memberId = connectToMember(blockingStub);
                    if (StringUtils.isNotBlank(memberId)) {
                        connection = new GrpcConnection(memberId, blockingStub);
                        member.setMemberId(memberId);
                        member.setConnection(connection);
                        member.setStatus(NodeStatus.UP);
                    }
                } catch (Exception e) {
                    // nothing to do
                    if (channel != null) {
                        channel.awaitTermination(100, TimeUnit.MICROSECONDS);
                    }
                    member.setStatus(NodeStatus.DOWN);
                    LOGGER.warn("Cannot connect to member address: " + member.getIp() + ":" + member.getPort());
                }
                countDownLatch.countDown();
                return connection;
            });
            // avoid gc
            connections.add(future);
        });
        try {
            countDownLatch.await(2000, TimeUnit.MICROSECONDS);
        } catch (InterruptedException e) {
            // nothing to do
        }
    }
    
    @Override
    public void refreshActiveTime(String id) {
        raftMemberManager.refreshMemberActive(id);
    }
}
