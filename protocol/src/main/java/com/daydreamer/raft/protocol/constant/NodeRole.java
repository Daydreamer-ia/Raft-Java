package com.daydreamer.raft.protocol.constant;

/**
 * @author Daydreamer
 *
 * status of member node
 */
public enum NodeRole {
    
    /**
     * leader
     */
    LEADER,
    
    /**
     * follower
     */
    FOLLOWER,
    
    /**
     * candidate
     */
    CANDIDATE
    
}
