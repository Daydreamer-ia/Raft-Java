package com.daydreamer.raft.protocol.core;

import com.daydreamer.raft.protocol.entity.RaftConfig;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Daydreamer
 * <p>
 * It is a manager to retain connection
 */
public abstract class AbstractFollowerNotifier {
    
    /**
     * member manager
     */
    protected RaftMemberManager raftMemberManager;
    
    /**
     * config
     */
    protected RaftConfig raftConfig;
    
    /**
     * whether init
     */
    protected AtomicBoolean isInit = new AtomicBoolean(false);
    
    /**
     * executor for schedule
     */
    protected ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, r -> {
        Thread thread = new Thread(r);
        thread.setName("refresh-active-time-thread");
        thread.setDaemon(true);
        return thread;
    });
    
    public AbstractFollowerNotifier(RaftMemberManager raftMemberManager, RaftConfig raftConfig) {
        this.raftMemberManager = raftMemberManager;
        this.raftConfig = raftConfig;
    }
    
    /**
     * init notifier
     */
    public synchronized void init() {
        if (isInit.get()) {
            return;
        }
        executor.execute(this::keepFollowers);
        // other init operation
        doInit();
        // finish
        isInit.set(true);
    }
    
    /**
     * init
     */
    protected void doInit() {
        // nothing to do default
    }
    
    /**
     * remind follower to keep current node as leader
     *
     */
    protected abstract void keepFollowers();
}
