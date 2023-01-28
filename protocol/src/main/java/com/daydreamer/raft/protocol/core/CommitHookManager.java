package com.daydreamer.raft.protocol.core;

import com.daydreamer.raft.api.callback.CommitHook;
import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.common.annotation.SPIMethodInit;
import com.daydreamer.raft.common.filter.LogFilter;
import com.daydreamer.raft.common.loader.GroupAware;
import com.daydreamer.raft.common.loader.RaftServiceLoader;
import com.daydreamer.raft.protocol.chain.LogPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Daydreamer
 */
public abstract class CommitHookManager implements LogPostProcessor, GroupAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommitHookManager.class);

    protected String groupKey;

    protected final Map<Object, CommitHook> hooks = new ConcurrentHashMap<>();

    protected LogFilter logFilter;

    @SPIMethodInit
    private void init() {
        logFilter = RaftServiceLoader.getLoader(groupKey, LogFilter.class).getDefault();
    }

    /**
     * register new hook for key
     *
     * @param key  key
     * @param hook hook
     */
    public synchronized boolean register(Object key, CommitHook hook) {
        CommitHook commitHooks = hooks.get(key);
        if (commitHooks == null) {
            hooks.putIfAbsent(key, hook);
            return true;
        }
        return false;
    }

    /**
     * unregister hook
     *
     * @param key hook key
     */
    public synchronized void removeHook(Object key) {
        hooks.remove(key);
    }

    /**
     * commit log
     *
     * @param logEntry log
     */
    public abstract void commit(LogEntry logEntry);

    @Override
    public boolean handleBeforeAppend(LogEntry logEntry) {
        // nothing to do
        return true;
    }

    @Override
    public void handleAfterAppend(LogEntry logEntry) {
        // nothing to do
    }

    @Override
    public void handleAfterCommit(LogEntry logEntry) {
        try {
            // if it does not allow to filter
            if (logFilter.filter(logEntry)) {
                commit(logEntry);
            }
        } catch (Throwable e) {
            LOGGER.error("Fail to do hooks for committed log: " + logEntry + ", "
                    + "because: " + e.getLocalizedMessage());
        }
    }

    @Override
    public boolean handleBeforeCommit(LogEntry logEntry) {
        // nothing to do
        return true;
    }

    @Override
    public void setGroupKey(String key) {
        this.groupKey = key;
    }
}
