package com.daydreamer.raft.api.callback;

import com.daydreamer.raft.api.entity.base.LogEntry;

/**
 * @author Daydreamer
 * <p>
 * it will be invoked after log committed
 */
public interface CommitHook {

    /**
     * it will be invoked after log committed
     *
     * @param logEntry committed log
     */
    void handleCommittedLog(LogEntry logEntry);
}
