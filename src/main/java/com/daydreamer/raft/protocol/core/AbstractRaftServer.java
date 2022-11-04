package com.daydreamer.raft.protocol.core;

import com.daydreamer.raft.protocol.constant.NodeRole;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.protocol.entity.RaftConfig;
import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.protocol.handler.RequestHandlerHolder;
import com.daydreamer.raft.transport.connection.Closeable;
import com.daydreamer.raft.transport.entity.request.HeartbeatRequest;
import com.daydreamer.raft.transport.entity.response.HeartbeatResponse;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * @author Daydreamer
 */
public abstract class AbstractRaftServer implements Closeable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRaftServer.class);
    
    /**
     * if there is a leader in cluster
     */
    protected AtomicBoolean normalCluster = new AtomicBoolean(false);
    
    /**
     * last time when leader active
     *
     * update if current node is follower and receive leader heartbeat
     */
    protected volatile long leaderLastActiveTime;
    
    /**
     * to be candidate start time
     *
     * update if current node is follower and receive other node vote request
     */
    protected volatile long beCandidateStartTime;
    
    /**
     * raft config
     */
    protected RaftConfig raftConfig;
    
    /**
     * vote executor
     */
    private ExecutorService executorService = new ThreadPoolExecutor(1, 1, 1000, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<>(), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("Ask-Vote-Thread");
            thread.setDaemon(true);
            return thread;
        }
    });
    
    public AbstractRaftServer(RaftConfig raftConfig) {
        this.raftConfig = raftConfig;
    }
    
    /**
     * init status of server
     */
    public void start() {
        try {
            // start server
            doStartServer();
            // register heart beat request handler
            RequestHandlerHolder.register(new RequestHandler<HeartbeatRequest, HeartbeatResponse>() {
                
                @Override
                public HeartbeatResponse handle(HeartbeatRequest request) {
                    // renew
                    refreshLeaderActive();
                    return new HeartbeatResponse();
                }
    
                @Override
                public Class<HeartbeatRequest> getSource() {
                    return HeartbeatRequest.class;
                }
            });
            // init job to vote
            initAskVoteLeaderJob();
        } catch (Exception e) {
            throw new IllegalStateException("Fail to start raft server, because " + e.getLocalizedMessage());
        }
    }
    
    /**
     * whether current node has just one leader
     *
     * @return whether current node has just one leader
     */
    public boolean normalCluster() {
        return normalCluster.get();
    }
    
    /**
     * ask votes to be leader
     *
     * @throws Exception
     */
    private void initAskVoteLeaderJob() {
        executorService.execute(() -> {
            try {
                while (true) {
                    // wait a random time
                    int waitTime = raftConfig.getVoteBaseTime() + new Random().nextInt(raftConfig.getVoteBaseTime() / 2);
                    LockSupport.parkNanos(TimeUnit.MICROSECONDS.toNanos(waitTime));
                    // No election will be held if the following conditions are met:
                    //   if current node is leader
                    //   if cluster has leader base on leaderLastActiveTime variable
                    //   if current node receive a vote request from other in this term
                    if (isLeader()) {
                        continue;
                    }
                    boolean leaderHeartbeatTimeout = NodeRole.FOLLOWER.equals(getSelf().getRole()) && System.currentTimeMillis() - leaderLastActiveTime > raftConfig.getAbnormalActiveInterval();
                    boolean candidateWaitTimeout = NodeRole.CANDIDATE.equals(getSelf().getRole()) && System.currentTimeMillis() - beCandidateStartTime > raftConfig.getCandidateStatusTimeout();
                    if (leaderHeartbeatTimeout || candidateWaitTimeout) {
                        // return the val whether don't need to allow to vote again
                        // current may be leader
                        if (requestVote()) {
                            getSelf().setRole(NodeRole.LEADER);
                            normalCluster.compareAndSet(false, true);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("[AbstractRaftServer] - Fail to do vote, because " + e.getLocalizedMessage());
            }
        });
    }
    
    /**
     * get self
     *
     * @return self
     */
    protected abstract Member getSelf();
    
    /**
     * start server
     */
    protected abstract void doStartServer();
    
    /**
     * request for leader
     *
     * @return whether current node being leader
     * @throws Exception
     */
    public abstract boolean requestVote() throws Exception;
    
    /**
     * whether current node is leader
     *
     * @return whether current node is leader
     */
    public abstract boolean isLeader();
    
    /**
     * refresh if leader active
     */
    public void refreshLeaderActive() {
        leaderLastActiveTime = System.currentTimeMillis();
        normalCluster.compareAndSet(true, false);
    }
    
}
