package com.daydreamer.raft.protocol.core;

import com.daydreamer.raft.protocol.entity.RaftConfig;
import com.daydreamer.raft.transport.connection.Closeable;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
     */
    protected long leaderLastActiveTime;
    
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
    private void askVoteLeader() {
        executorService.execute(() -> {
            try {
                while (true) {
                    // No election will be held if the following conditions are met:
                    // if current node is leader
                    // if cluster has leader base on normalCluster variable
                    if (!(isLeader() || System.currentTimeMillis() - leaderLastActiveTime > raftConfig.getAbnormalActiveInterval())) {
                        normalCluster.compareAndSet(false, true);
                        requestVote();
                    }
                }
            } catch (Exception e) {
                LOGGER.error("[AbstractRaftServer] - Fail to do vote, because " + e.getLocalizedMessage());
            }
        });
    }
    
    /**
     * start server
     */
    protected abstract void doStartServer();
    
    /**
     * request for leader
     *
     * @return whether current node being leader
     */
    public abstract boolean requestVote();
    
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
