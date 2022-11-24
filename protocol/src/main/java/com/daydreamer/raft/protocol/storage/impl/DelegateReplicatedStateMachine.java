package com.daydreamer.raft.protocol.storage.impl;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.exception.LogException;
import com.daydreamer.raft.protocol.storage.ReplicatedStateMachine;

import org.apache.log4j.Logger;

/**
 * @author Daydreamer
 */
public class DelegateReplicatedStateMachine implements ReplicatedStateMachine {
    
    private static final Logger LOGGER = Logger.getLogger(DelegateReplicatedStateMachine.class);
    
    /**
     * raftMemberManager
     */
    private RaftMemberManager raftMemberManager;
    
    /**
     * storageRepository
     */
    private ReplicatedStateMachine replicatedStateMachine;
    
    public DelegateReplicatedStateMachine(RaftMemberManager raftMemberManager, ReplicatedStateMachine replicatedStateMachine) {
        this.raftMemberManager = raftMemberManager;
        this.replicatedStateMachine = replicatedStateMachine;
    }
    
    @Override
    public boolean commit(int term, long logId) throws LogException {
        boolean commit = replicatedStateMachine.commit(term, logId);
        if (commit) {
            LOGGER.info("Member: "+ raftMemberManager.getSelf().getAddress() + ", "+ replicatedStateMachine.getLogById(logId)  +  " commit finish!");
        }
        return commit;
    }
    
    @Override
    public boolean append(LogEntry logEntry) throws LogException {
        boolean append = replicatedStateMachine.append(logEntry);
        if (append) {
            LOGGER.info("Member: "+ raftMemberManager.getSelf().getAddress() + ", "+ logEntry  +  " append finish!");
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
