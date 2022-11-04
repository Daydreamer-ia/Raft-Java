package com.daydreamer.raft.protocol.core;

/**
 * @author Daydreamer
 *
 * protocol
 */
public interface Protocol {
    
    /**
     * write
     */
    void write();
    
    /**
     * read
     */
    void read();
    
    /**
     * start
     */
    void run();
}
