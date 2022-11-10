package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.api.entity.request.EntryCommittedRequest;
import com.daydreamer.raft.api.entity.response.EntryCommittedResponse;
import com.daydreamer.raft.protocol.aware.StorageRepositoryAware;
import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.protocol.storage.StorageRepository;

/**
 * @author Daydreamer
 */
public class LogCommittedRequestHandler
        implements RequestHandler<EntryCommittedRequest, EntryCommittedResponse>, StorageRepositoryAware {
    
    private StorageRepository storageRepository;
    
    @Override
    public synchronized EntryCommittedResponse handle(EntryCommittedRequest request) {
        long logId = request.getLogId();
        int term = request.getTerm();
        if (storageRepository.getLastCommittedLogId() >= request.getLogId()) {
            LogEntry logEntry = storageRepository.getLogById(logId);
            if (logEntry != null && logEntry.getTerm() == term) {
                return new EntryCommittedResponse(true);
            }
        }
        return new EntryCommittedResponse(false);
    }
    
    @Override
    public Class<EntryCommittedRequest> getSource() {
        return EntryCommittedRequest.class;
    }
    
    @Override
    public void setStorageRepository(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }
}
