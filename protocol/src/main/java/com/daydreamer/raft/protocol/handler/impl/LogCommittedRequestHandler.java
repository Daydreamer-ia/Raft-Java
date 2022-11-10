package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.api.entity.Response;
import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.api.entity.constant.ResponseCode;
import com.daydreamer.raft.api.entity.request.EntryCommittedRequest;
import com.daydreamer.raft.api.entity.response.EntryCommittedResponse;
import com.daydreamer.raft.api.entity.response.ServerErrorResponse;
import com.daydreamer.raft.protocol.aware.StorageRepositoryAware;
import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.protocol.storage.StorageRepository;

import java.util.logging.Logger;

/**
 * @author Daydreamer
 */
public class LogCommittedRequestHandler
        implements RequestHandler<EntryCommittedRequest, Response>, StorageRepositoryAware {
    
    private static final Logger LOGGER = Logger.getLogger(LogCommittedRequestHandler.class.getSimpleName());
    
    private StorageRepository storageRepository;
    
    @Override
    public synchronized Response handle(EntryCommittedRequest request) {
        try {
            long logId = request.getLogId();
            int term = request.getTerm();
            if (storageRepository.getLastUncommittedLogId() >= request.getLogId()) {
                LogEntry logEntry = storageRepository.getLogById(logId);
                if (logEntry != null && logEntry.getTerm() == term) {
                    storageRepository.commit(term, logId);
                    return new EntryCommittedResponse(true);
                }
            }
            return new EntryCommittedResponse(false);
        } catch (Exception e) {
            LOGGER.severe("[LogCommittedRequestHandler] - Fail to commit log, because: "+ e.getMessage());
            return new ServerErrorResponse(e.getMessage(), ResponseCode.ERROR_SERVER);
        }
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
