package com.daydreamer.raft.protocol.storage.impl;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.protocol.chain.LogPostProcessorHolder;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.exception.LogException;
import com.daydreamer.raft.protocol.storage.ReplicatedStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


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
        // no uncommitted log
        if (getLastUncommittedLogId() == -1) {
            return false;
        }
        // has committed
        if (getLastCommittedLogId() == logId) {
            return true;
        }
        // get uncommitted log until logId
        List<LogEntry> logReadyToCommit = getUncommittedLogUntil(logId);
        boolean continueOp = true;
        for (int i = 0; continueOp && i < logReadyToCommit.size(); i++) {
            continueOp = logPostProcessorHolder.handleBeforeCommit(logReadyToCommit.get(i));
        }
        // if false
        if (!continueOp) {
            return false;
        }
        // get log ready to commit
        boolean commit = replicatedStateMachine.commit(term, logId);
        if (commit) {
            for (LogEntry log : logReadyToCommit) {
                logPostProcessorHolder.handleAfterCommit(log);
            }
            LOGGER.info("Member: " + raftMemberManager.getSelf().getAddress() + ", " + replicatedStateMachine
                    .getLogById(logId) + " commit finish!");
        }
        return commit;
    }
    
    /**
     * get log until logIndex
     *
     * @param unCommittedLogIndex last log index
     * @return log entries
     */
    private List<LogEntry> getUncommittedLogUntil(long unCommittedLogIndex) {
        List<LogEntry> logEntries = new ArrayList<>();
        long logIndex = getLastCommittedLogId() + 1;
        while (logIndex <= unCommittedLogIndex) {
            try {
                logEntries.add(getLogById(logIndex));
            } catch (Exception e) {
                LOGGER.info("Fail to get log, because {}, log index: {}", e.getMessage(), logIndex);
            }
            logIndex++;
        }
        return logEntries;
    }
    
    @Override
    public boolean append(LogEntry logEntry) throws LogException {
        long lastUncommittedLogIndex = getLastUncommittedLogId();
        // filter
        if (!logPostProcessorHolder.handleBeforeAppend(logEntry)) {
            return false;
        }
        boolean append = replicatedStateMachine.append(logEntry);
        if (append) {
            long logIndex = lastUncommittedLogIndex + 1;
            while (logIndex <= getLastUncommittedLogId()) {
                logPostProcessorHolder.handleAfterAppend(logEntry);
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
