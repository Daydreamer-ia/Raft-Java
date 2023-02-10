package com.daydreamer.raft.protocol.chain;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.common.annotation.SPI;

/**
 * @author Daydreamer
 *
 * LogEntry -append-> LogPostProcessor::handleAfterAppend -commit-> LogPostProcessor::handleAfterCommit
 */
@SPI("logPostProcessor")
public interface LogPostProcessor {
    
    /**
     * post process log entry
     *
     * @param logEntry log entry
     * @return whether continue to append
     */
    default boolean handleBeforeAppend(LogEntry logEntry) {
        return true;
    }
    
    /**
     * post process log entry
     *
     * @param logEntry log entry
     */
    default void handleAfterAppend(LogEntry logEntry) {

    }
    
    /**
     * post process log entry
     *
     * @param logEntry log entry
     */
    default void handleAfterCommit(LogEntry logEntry) {

    }
    
    /**
     * post process log entry
     *
     * @param logEntry log entry
     * @return whether continue to commit
     */
    default boolean handleBeforeCommit(LogEntry logEntry) {
        return true;
    }
    
}
