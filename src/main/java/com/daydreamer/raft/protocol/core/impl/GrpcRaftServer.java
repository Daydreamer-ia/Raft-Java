package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.core.AbstractRaftServer;

/**
 * @author Daydreamer
 */
public class GrpcRaftServer extends AbstractRaftServer {
    
    public GrpcRaftServer(RaftMemberManager raftMemberManager) {
        super(raftMemberManager);
    }
}
