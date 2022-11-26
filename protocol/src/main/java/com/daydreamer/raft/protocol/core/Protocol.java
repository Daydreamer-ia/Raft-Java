package com.daydreamer.raft.protocol.core;

import com.daydreamer.raft.api.entity.base.MemberChangeEntry;
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
     * member change
     *
     * @param memberChangeEntry which member change
     * @throws Exception Exception
     * @return whether change successfully
     */
    boolean memberChange(MemberChangeEntry memberChangeEntry) throws Exception;
    
    /**
     * start
     */
    void run();
    
    /**
     * stop protocol running
     */
    void close();
}
