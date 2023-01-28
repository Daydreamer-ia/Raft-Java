package com.daydreamer.raft.protocol.core;

import com.daydreamer.raft.api.callback.CommitHook;
import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.protocol.chain.LogPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Daydreamer
 */
public abstract class CommitHookManager implements LogPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommitHookManager.class);

    protected final Map<Object, Set<CommitHook>> hooks = new ConcurrentHashMap<>();

    /**
     * register new hook for key
     *
     * @param key  key
     * @param hook hook
     */
    public void register(Object key, CommitHook hook) {
        Set<CommitHook> commitHooks = hooks.get(key);
        if (commitHooks == null) {
            hooks.putIfAbsent(key, new CopyOnWriteArraySet<>());
        }
        commitHooks = hooks.get(key);
        commitHooks.add(hook);
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
            commit(logEntry);
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
}
