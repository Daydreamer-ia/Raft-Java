package com.daydreamer.raft.protocol.aware;

import com.daydreamer.raft.protocol.storage.StorageRepository;

/**
 * @author Daydreamer
 */
public interface StorageRepositoryAware {
    
    /**
     * setter for StorageRepositoryAware
     *
     * @param storageRepositoryAware StorageRepositoryAware
     */
    void setStorageRepository(StorageRepository storageRepositoryAware);
}
