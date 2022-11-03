package com.daydreamer.raft.protocol.core;

import com.daydreamer.raft.transport.connection.Connection;

/**
 * @author Daydreamer
 * <p>
 * It is a manager to retain connection
 */
public interface ConnectionManager {
    
    /**
     * init
     */
    void init();
    
    /**
     * refresh active time
     *
     * @param id conn id
     */
    void refreshActiveTime(String id);
}
