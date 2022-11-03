package com.daydreamer.raft.protocol.core;

import com.daydreamer.raft.protocol.constant.NodeRole;
import com.daydreamer.raft.protocol.constant.NodeStatus;
import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.protocol.entity.RaftConfig;

import java.net.InetAddress;
import java.util.UUID;

/**
 * @author Daydreamer
 */
public abstract class AbstractRaftServer {
    
    /**
     * raft member manager
     */
    private RaftMemberManager raftMemberManager;
    
    /**
     * raft config
     */
    private RaftConfig raftConfig = new RaftConfig();
    
    /**
     * current node
     */
    private Member self;
    
    /**
     * if there is a leader in cluster
     */
    private boolean normalCluster = false;
    
    public AbstractRaftServer(RaftMemberManager raftMemberManager) {
        this.raftMemberManager = raftMemberManager;
    }
    
    /**
     * init status of server
     */
    private void start() {
        try {
            // init current cluster
            self = initSelf();
            // init stub
            
            
        } catch (Exception e) {
            throw new IllegalStateException("Fail to start raft server, because " + e.getLocalizedMessage());
        }
    }
    
    /**
     * init current node member
     *
     * @return current node member
     * @throws Exception
     */
    public Member initSelf() throws Exception {
        String ip = InetAddress.getLocalHost().getHostAddress();
        int port = raftConfig.getPort();
        Member self = new Member();
        self.setIp(ip);
        self.setPort(port);
        self.setAddress(ip + ":" + port);
        self.setRole(NodeRole.CANDIDATE, null);
        self.setMemberId(UUID.randomUUID().toString());
        self.setStatus(NodeStatus.UP);
        self.setTerm(0);
        self.setLogId(0);
        return self;
    }
    
    /**
     * get unique modifier as id in cluster, which represents the current node
     *
     * @return id
     */
    public String getModifier() {
        return UUID.randomUUID().toString();
    }
    
}
