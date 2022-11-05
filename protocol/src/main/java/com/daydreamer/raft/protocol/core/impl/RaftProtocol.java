package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.common.service.PropertiesReader;
import com.daydreamer.raft.protocol.core.AbstractRaftServer;
import com.daydreamer.raft.protocol.core.RaftMemberManager;
import com.daydreamer.raft.protocol.entity.RaftConfig;
import com.daydreamer.raft.protocol.core.Protocol;

/**
 * @author Daydreamer
 *
 * raft protocol
 */
public class RaftProtocol implements Protocol {
    
    private AbstractRaftServer raftServer;
    
    public RaftProtocol(String raftConfigPath) {
        // init reader
        PropertiesReader<RaftConfig> reader = new RaftPropertiesReader(raftConfigPath);
        RaftMemberManager raftMemberManager = new MemberManager(reader.getProperties());
        this.raftServer = new GrpcRaftServer(raftConfigPath,
                raftMemberManager, new GrpcFollowerNotifier(raftMemberManager, reader.getProperties()));
    }
    
    @Override
    public void write() {
        throw new UnsupportedOperationException("Current version don't support this action.");
    }
    
    @Override
    public void read() {
        throw new UnsupportedOperationException("Current version don't support this action.");
    }
    
    @Override
    public void run() {
        raftServer.start();
    }
}
