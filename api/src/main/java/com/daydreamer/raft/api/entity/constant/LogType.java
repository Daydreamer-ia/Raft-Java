package com.daydreamer.raft.api.entity.constant;

/**
 * @author Daydreamer
 * <p>
 * type of raft log
 */
public enum LogType {
    
    /**
     * read log
     */
    READ,
    
    /**
     * write log
     */
    WRITE,
    
    /**
     * no-op
     */
    NO_OP,
    
    /**
     * member change
     */
    MEMBER_CHANGE
}
