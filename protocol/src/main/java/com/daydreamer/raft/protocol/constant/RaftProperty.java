package com.daydreamer.raft.protocol.constant;

/**
 * @author Daydreamer
 */
public class RaftProperty {
    
    private RaftProperty() {}
    
    public static final String MEMBER_ADDRESSES = "ddr.raft.members-addresses";
    
    public static final String LEADER_HEARTBEAT = "ddr.raft.heartInterval";
    
    public static final String ABNORMAL_LEADER_ACTIVE_INTERNAL = "ddr.raft.leader-inactive-timeout";
    
    public static final String VOTE_BASE_TIME = "ddr.raft.voteBaseTime";
    
    public static final String CANDIDATE_WAIT_TIMEOUT = "ddr.raft.candidate-wait-timeout";
    
    public static final String WRITE_RETRY_TIMES_IF_FAIL = "ddr.raft.write-retry-times";
    
    public static final String SERVER_ADDR = "ddr.raft.server-address";
    
}
