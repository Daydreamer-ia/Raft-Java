package com.daydreamer.raft.protocol.aware;

import com.daydreamer.raft.protocol.core.AbstractRaftServer;

/**
 * @author Daydreamer
 */
public interface RaftServerAware {
    
    /**
     * setter for RaftServer
     *
     * @param raftServer raftServer
     */
    void setRaftServer(AbstractRaftServer raftServer);
}
