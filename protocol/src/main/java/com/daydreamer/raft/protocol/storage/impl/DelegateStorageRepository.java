package com.daydreamer.raft.protocol.storage.impl;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.exception.LogException;
import com.daydreamer.raft.protocol.storage.StorageRepository;

import java.util.logging.Logger;

/**
 * @author Daydreamer
 */
public class DelegateStorageRepository implements StorageRepository {
    
    private static final Logger LOGGER = Logger.getLogger(DelegateStorageRepository.class.getSimpleName());
    
    /**
     * raftMemberManager
     */
    private RaftMemberManager raftMemberManager;
    
    /**
     * storageRepository
     */
    private StorageRepository storageRepository;
    
    public DelegateStorageRepository(RaftMemberManager raftMemberManager, StorageRepository storageRepository) {
        this.raftMemberManager = raftMemberManager;
        this.storageRepository = storageRepository;
    }
    
    @Override
    public boolean commit(int term, long logId) throws LogException {
        boolean commit = storageRepository.commit(term, logId);
        if (commit) {
            raftMemberManager.getSelf().setLogId(logId);
            LOGGER.info("Member: "+ raftMemberManager.getSelf().getAddress() + ", term: " + term + ", log index: " + logId+ " has committed!");
        }
        return commit;
    }
    
    @Override
    public boolean append(LogEntry logEntry) throws LogException {
        boolean append = storageRepository.append(logEntry);
        if (append) {
            LOGGER.info("Member: "+ raftMemberManager.getSelf().getAddress() + ", term: " + logEntry.getTerm() + ", log index: " + logEntry.getLogId()
                    + " append finish!");
        }
        return append;
    }
    
    @Override
    public LogEntry getCommittedLog(long logId) {
        return storageRepository.getCommittedLog(logId);
    }
    
    @Override
    public LogEntry getUncommittedLog(long logId) {
        return storageRepository.getUncommittedLog(logId);
    }
    
    @Override
    public long getLastCommittedLogId() {
        return storageRepository.getLastCommittedLogId();
    }
    
    @Override
    public LogEntry getLogById(long logId) {
        return storageRepository.getLogById(logId);
    }
    
    @Override
    public long getLastUncommittedLogId() {
        return storageRepository.getLastUncommittedLogId();
    }
    
    @Override
    public void close() {
        storageRepository.close();
    }
}
