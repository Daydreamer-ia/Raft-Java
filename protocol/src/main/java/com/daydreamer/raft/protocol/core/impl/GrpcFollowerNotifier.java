package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.protocol.constant.NodeStatus;
import com.daydreamer.raft.protocol.core.AbstractFollowerNotifier;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.entity.RaftConfig;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.transport.connection.Connection;
import com.daydreamer.raft.transport.connection.ResponseCallBack;
import com.daydreamer.raft.api.entity.Response;
import com.daydreamer.raft.api.entity.request.HeartbeatRequest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author Daydreamer
 * <p>
 * It is a implmement to retain grpc connection.
 */
public class GrpcFollowerNotifier extends AbstractFollowerNotifier {
    
    private static final Logger LOGGER = Logger.getLogger(GrpcFollowerNotifier.class.getSimpleName());
    
    public GrpcFollowerNotifier(RaftMemberManager raftMemberManager, RaftConfig raftConfig) {
        super(raftMemberManager, raftConfig);
    }
    
    @Override
    public void keepFollowers() {
        try {
            // if current node is leader
            // tell follower to keep
            if (raftMemberManager.isLeader()) {
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
                                Member self = raftMemberManager.getSelf();
                                connection.request(new HeartbeatRequest(self.getTerm(), self.getLogId()),
                                        2000, new ResponseCallBack() {
                                
                                    @Override
                                    public void onSuccess(Response response) {
                                        member.setStatus(NodeStatus.UP);
                                        countDownLatch.countDown();
                                    }
                                
                                    @Override
                                    public void onFail(Exception e) {
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
                        LOGGER.severe("[GrpcConnectionManager] - Schedule error when check connection, because " + e
                                .getLocalizedMessage() + ". current member" + member.getAddress());
                    }
                });
                // help gc
                allMember = null;
                // wait for response
                countDownLatch.await(3000L, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            // nothing to do
            e.printStackTrace();
        }
    }
}