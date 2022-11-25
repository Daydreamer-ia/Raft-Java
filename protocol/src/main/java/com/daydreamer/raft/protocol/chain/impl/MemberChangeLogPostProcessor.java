package com.daydreamer.raft.protocol.chain.impl;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.protocol.chain.LogPostProcessor;

/**
 * @author Daydreamer
 * <p>
 * handle if member change log append or commit
 */
public class MemberChangeLogPostProcessor implements LogPostProcessor {
    
    @Override
    public void handleAfterAppend(LogEntry logEntry) {
    
    }
    
    @Override
    public void handleAfterCommit(LogEntry logEntry) {
    
    }
}
