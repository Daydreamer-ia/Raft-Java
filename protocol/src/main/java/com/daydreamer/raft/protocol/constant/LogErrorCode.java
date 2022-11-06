package com.daydreamer.raft.protocol.constant;

/**
 * @author Daydreamer
 */
public class LogErrorCode {
    
    private LogErrorCode() {}
    
    /**
     * cause when commit log but last uncommitted log id is too less
     */
    public static final int UNCOMMITTED_LOG_TO_LESS = 1;
    
}
