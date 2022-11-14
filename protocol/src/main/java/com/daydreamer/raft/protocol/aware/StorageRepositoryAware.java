package com.daydreamer.raft.protocol.aware;

import com.daydreamer.raft.protocol.storage.ReplicatedStateMachine;

/**
 * @author Daydreamer
 */
public interface StorageRepositoryAware {
    
    /**
     * setter for StorageRepositoryAware
     *
     * @param replicatedStateMachine StorageRepository
     */
    void setReplicatedStateMachine(ReplicatedStateMachine replicatedStateMachine);
}
