package com.daydreamer.raft.protocol.core;

import com.daydreamer.raft.common.annotation.SPI;
import com.daydreamer.raft.common.annotation.SPIMethodInit;
import com.daydreamer.raft.common.entity.RaftConfig;
import com.daydreamer.raft.transport.connection.Closeable;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Daydreamer
 * <p>
 * It is a manager to retain connection
 */
@SPI("abstractFollowerNotifier")
public abstract class AbstractFollowerNotifier implements Closeable {
    
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

    /**
     * init notifier
     */
    @SPIMethodInit
    public synchronized void init() {
        if (isInit.get()) {
            return;
        }
        executor.scheduleAtFixedRate(this::keepFollowers, raftConfig.getHeartInterval(), raftConfig.getHeartInterval(), TimeUnit.MICROSECONDS);
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
