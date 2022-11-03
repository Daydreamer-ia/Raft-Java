package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.protocol.constant.NodeStatus;
import com.daydreamer.raft.protocol.core.ConnectionManager;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.protocol.entity.RaftConfig;
import com.daydreamer.raft.transport.connection.Connection;
import com.daydreamer.raft.transport.connection.ResponseCallBack;
import com.daydreamer.raft.transport.entity.Response;
import com.daydreamer.raft.transport.entity.request.HeartbeatRequest;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
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
                                    }
    
                                    @Override
                                    public void onTimeout() {
                                        countDownLatch.countDown();
                                    }
                                });
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
            } catch (Exception e) {
                // nothing to do
                e.printStackTrace();
            }
        };
        int random = new Random().nextInt(raftConfig.getHeartInterval() / 2);
        // submit job
        executor.scheduleAtFixedRate(job, raftConfig.getHeartInterval() + random, random, TimeUnit.MICROSECONDS);
    }
    
    @Override
    public void refreshActiveTime(String id) {
        raftMemberManager.refreshMemberActive(id);
    }
}
