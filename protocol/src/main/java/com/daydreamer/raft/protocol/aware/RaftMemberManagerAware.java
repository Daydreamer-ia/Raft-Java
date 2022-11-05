package com.daydreamer.raft.protocol.aware;

import com.daydreamer.raft.protocol.core.RaftMemberManager;

/**
 * @author Daydreamer
 */
public interface RaftMemberManagerAware {
    
    /**
     * setter for RaftMemberManagerAware
     *
     * @param raftMemberManager raftMemberManager
     */
    void setRaftMemberManager(RaftMemberManager raftMemberManager);
}
