package com.daydreamer.raft.protocol.constant;

/**
 * @author Daydreamer
 *
 * type of raft log
 */
public enum  LogType {
    
    /**
     * read log
     */
    READ,
    
    /**
     * write log
     */
    WRITE,
    
    /**
     * member change
     */
    MEMBER_CHANGE
}
