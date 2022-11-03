package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.protocol.constant.NodeStatus;
import com.daydreamer.raft.protocol.core.ConnectionManager;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.protocol.entity.RaftConfig;
import com.daydreamer.raft.transport.connection.Connection;
import com.daydreamer.raft.transport.connection.ResponseCallBack;
import com.daydreamer.raft.transport.connection.impl.grpc.GrpcRequestServerCore;
import com.daydreamer.raft.transport.entity.Response;
import com.daydreamer.raft.transport.entity.request.HeartbeatRequest;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
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
    
    private Map<String, Member> memberMap = new ConcurrentHashMap<>();
    
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
                                    // nothing to do
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
            // try to reconnect
            unconnected.forEach(member -> {
            
            });
        };
        int random = new Random().nextInt(raftConfig.getHeartInterval() / 2);
        // submit job
        executor.scheduleAtFixedRate(job, raftConfig.getHeartInterval() + random, random, TimeUnit.MICROSECONDS);
    }
    
    @Override
    public void refreshActiveTime(String id) {
//        raftMemberManager.refreshMemberActive(id);
    }
}
