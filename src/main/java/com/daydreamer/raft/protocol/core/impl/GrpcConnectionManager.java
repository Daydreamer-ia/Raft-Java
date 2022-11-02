package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.protocol.core.ConnectionManager;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.protocol.entity.RaftConfig;
import com.daydreamer.raft.transport.connection.Connection;
import com.daydreamer.raft.transport.entity.Response;
import com.daydreamer.raft.transport.entity.request.HeartbeatRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author Daydreamer
 * <p>
 * It is a implmement to retain grpc connection.
 */
public class GrpcConnectionManager implements ConnectionManager {
    
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
            // TODO 连接检测
            List<Member> allMember = raftMemberManager.getAllMember();
            List<Future> outConnected = new ArrayList<>(allMember.size());
            allMember.forEach(member -> {
                try {
                    long lastActiveTime = member.getLastActiveTime();
                    long currentTime = System.currentTimeMillis();
                    // not active a period time
                    if (currentTime - lastActiveTime > raftConfig.getHeartInterval()) {
                        Connection connection = member.getConnection();
                        if (connection != null) {
                            Future<Response> responseFuture = connection.request(new HeartbeatRequest());
                            outConnected.add(responseFuture);
                        }
                    }
                } catch (Exception e) {
                
                }
            });
        };
    }
    
    @Override
    public void register(Connection connection) {
    
    }
    
    @Override
    public void deregister(String id) {
    
    }
    
    @Override
    public void refreshActiveTime(String id) {
    
    }
}
