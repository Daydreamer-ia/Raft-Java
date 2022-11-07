package com.daydreamer.raft.protocol.storage.impl;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.exception.LogException;
import com.daydreamer.raft.protocol.storage.StorageRepository;

/**
 * @author Daydreamer
 */
public class DelegateStorageRepository implements StorageRepository {
    
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
        }
        return commit;
    }
    
    @Override
    public boolean append(LogEntry logEntry) throws LogException {
        return storageRepository.append(logEntry);
    }
    
    @Override
    public LogEntry getLog(long logId) {
        return storageRepository.getLog(logId);
    }
    
    @Override
    public long getLastCommittedLogId() {
        return storageRepository.getLastCommittedLogId();
    }
    
    @Override
    public long getLastUncommittedLogId() {
        return storageRepository.getLastUncommittedLogId();
    }
}
