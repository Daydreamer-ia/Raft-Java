package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.api.entity.request.AppendEntriesRequest;
import com.daydreamer.raft.api.entity.response.AppendEntriesResponse;
import com.daydreamer.raft.protocol.aware.StorageRepositoryAware;
import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.protocol.storage.StorageRepository;

/**
 * @author Daydreamer
 */
public class AppendEntriesRequestHandler
        implements RequestHandler<AppendEntriesRequest, AppendEntriesResponse>, StorageRepositoryAware {
    
    /**
     * log repository
     */
    private StorageRepository storageRepository;
    
    @Override
    public synchronized AppendEntriesResponse handle(AppendEntriesRequest request) {
        try {
            // check log id and term
            
            // append
            
        } catch (Exception e) {
            // nothing to do
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public Class<AppendEntriesRequest> getSource() {
        return AppendEntriesRequest.class;
    }
    
    @Override
    public void setStorageRepository(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }
}
