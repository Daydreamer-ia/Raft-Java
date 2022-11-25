package com.daydreamer.raft.protocol.storage.impl;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.protocol.chain.LogPostProcessor;
import com.daydreamer.raft.protocol.chain.LogPostProcessorHolder;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.exception.LogException;
import com.daydreamer.raft.protocol.storage.ReplicatedStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Daydreamer
 */
public class DelegateReplicatedStateMachine implements ReplicatedStateMachine {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DelegateReplicatedStateMachine.class);
    
    /**
     * raftMemberManager
     */
    private RaftMemberManager raftMemberManager;
    
    /**
     * storageRepository
     */
    private ReplicatedStateMachine replicatedStateMachine;
    
    /**
     * logPostProcessorHolder
     */
    private LogPostProcessorHolder logPostProcessorHolder;
    
    public DelegateReplicatedStateMachine(RaftMemberManager raftMemberManager,
            ReplicatedStateMachine replicatedStateMachine, LogPostProcessorHolder logPostProcessorHolder) {
        this.raftMemberManager = raftMemberManager;
        this.replicatedStateMachine = replicatedStateMachine;
        this.logPostProcessorHolder = logPostProcessorHolder;
    }
    
    @Override
    public boolean commit(int term, long logId) throws LogException {
        long lastUncommittedLogIndex = getLastUncommittedLogId();
        boolean commit = replicatedStateMachine.commit(term, logId);
        if (commit) {
            long logIndex = lastUncommittedLogIndex + 1;
            while (logIndex <= getLastCommittedLogId()) {
                for (LogPostProcessor logPostProcessor : logPostProcessorHolder.getPostProcessors()) {
                    LogEntry log = null;
                    try {
                        log = getLogById(logIndex);
                        logPostProcessor.handleAfterCommit(log);
                    } catch (Exception e) {
                        LOGGER.info("Fail to post process after commit, because {}, log: {}", e.getMessage(), log);
                    }
                }
                logIndex++;
            }
            LOGGER.info("Member: " + raftMemberManager.getSelf().getAddress() + ", " + replicatedStateMachine
                    .getLogById(logId) + " commit finish!");
        }
        return commit;
    }
    
    @Override
    public boolean append(LogEntry logEntry) throws LogException {
        long lastUncommittedLogIndex = getLastUncommittedLogId();
        boolean append = replicatedStateMachine.append(logEntry);
        if (append) {
            long logIndex = lastUncommittedLogIndex + 1;
            while (logIndex <= getLastUncommittedLogId()) {
                for (LogPostProcessor logPostProcessor : logPostProcessorHolder.getPostProcessors()) {
                    LogEntry log = null;
                    try {
                        log = getLogById(logIndex);
                        logPostProcessor.handleAfterAppend(log);
                    } catch (Exception e) {
                        LOGGER.info("Fail to post process after commit, because {}, log: {}", e.getMessage(), log);
                    }
                }
                logIndex++;
            }
            LOGGER.info("Member: " + raftMemberManager.getSelf().getAddress() + ", " + logEntry + " append finish!");
        }
        return append;
    }
    
    @Override
    public LogEntry getCommittedLog(long logId) {
        return replicatedStateMachine.getCommittedLog(logId);
    }
    
    @Override
    public LogEntry getUncommittedLog(long logId) {
        return replicatedStateMachine.getUncommittedLog(logId);
    }
    
    @Override
    public long getLastCommittedLogId() {
        return replicatedStateMachine.getLastCommittedLogId();
    }
    
    @Override
    public LogEntry getLogById(long logId) {
        return replicatedStateMachine.getLogById(logId);
    }
    
    @Override
    public long getLastUncommittedLogId() {
        return replicatedStateMachine.getLastUncommittedLogId();
    }
    
    @Override
    public void close() {
        replicatedStateMachine.close();
    }
}
