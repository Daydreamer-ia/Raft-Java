package com.daydreamer.raft.protocol.core;

import com.daydreamer.raft.api.entity.base.Payload;

/**
 * @author Daydreamer
 * <p>
 * protocol
 */
public interface Protocol {
    
    /**
     * write
     *
     * @param payload data
     * @return whether committed
     * @throws Exception abnormal cluster
     */
    boolean write(Payload payload) throws Exception;
    
    /**
     * read
     */
    void read();
    
    /**
     * start
     */
    void run();
}
