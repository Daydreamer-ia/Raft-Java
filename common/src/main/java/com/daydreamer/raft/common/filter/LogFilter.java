package com.daydreamer.raft.common.filter;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.api.callback.CommitHook;
import com.daydreamer.raft.common.annotation.SPI;

/**
 * @author Daydreamer
 * <p>
 * it will be invoked after log committed, before {@link CommitHook}
 */
@SPI("filterChain")
public interface LogFilter {

    /**
     * filter log
     *
     * @param logEntry log
     * @return whether to filter current log
     */
    boolean filter(LogEntry logEntry);
}
