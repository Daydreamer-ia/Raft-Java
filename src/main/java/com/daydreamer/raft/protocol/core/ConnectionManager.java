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
     * register new connection
     *
     * @param connection new connection
     */
    void register(Connection connection);
    
    /**
     * deregister connection
     *
     * @param id conn id
     */
    void deregister(String id);
    
    /**
     * refresh active time
     *
     * @param id conn id
     */
    void refreshActiveTime(String id);
}
