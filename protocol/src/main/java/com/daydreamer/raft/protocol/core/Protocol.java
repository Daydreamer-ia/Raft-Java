package com.daydreamer.raft.protocol.core;

import com.daydreamer.raft.api.callback.CommitHook;
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
     * add the hook method to invoke after log committed
     *
     * @param key key
     * @param commitHook commitHook
     */
    boolean addListener(Object key, CommitHook commitHook);

    /**
     * remove the hook method to invoke after log committed
     *
     * @param key key
     */
    void removeListener(Object key);
    
    /**
     * start
     */
    void run();
    
    /**
     * stop protocol running
     */
    void close();
}
