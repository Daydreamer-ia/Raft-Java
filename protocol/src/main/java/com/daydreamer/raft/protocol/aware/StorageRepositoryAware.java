package com.daydreamer.raft.protocol.aware;

import com.daydreamer.raft.protocol.storage.StorageRepository;

/**
 * @author Daydreamer
 */
public interface StorageRepositoryAware {
    
    /**
     * setter for StorageRepositoryAware
     *
     * @param storageRepository StorageRepository
     */
    void setStorageRepository(StorageRepository storageRepository);
}
