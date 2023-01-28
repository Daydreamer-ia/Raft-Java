package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.api.callback.CommitHook;
import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.common.annotation.SPIMethodInit;
import com.daydreamer.raft.common.loader.GroupAware;
import com.daydreamer.raft.common.loader.RaftServiceLoader;
import com.daydreamer.raft.common.threadpool.ThreadPoolFactory;
import com.daydreamer.raft.protocol.core.CommitHookManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

/**
 * @author Daydreamer
 */
@SPIImplement("asynCommitHookManager")
public class AsynCommitHookManager extends CommitHookManager implements GroupAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsynCommitHookManager.class);

    private String groupKey;

    /**
     * job executor
     */
    private Executor executor;


    @SPIMethodInit
    private void init() {
        this.executor = RaftServiceLoader
                .getLoader(groupKey, ThreadPoolFactory.class)
                .getDefault()
                .getExecutor(AsynCommitHookManager.class);
    }

    @Override
    public void commit(LogEntry logEntry) {
        executor.execute(() -> {
            for (CommitHook commitHook : hooks.values()) {
                commitHook.handleCommittedLog(logEntry);
            }
        });
    }

    @Override
    public void setGroupKey(String key) {
        this.groupKey = key;
    }
}
