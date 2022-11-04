package com.daydreamer.raft.protocol.core;

/**
 * @author Daydreamer
 * <p>
 * It is a manager to retain connection
 */
public interface FollowerNotifier {
    
    /**
     * init
     */
    void init();
    
    /**
     * remind follower to keep current node as leader
     *
     */
    void keepFollowers();
}
