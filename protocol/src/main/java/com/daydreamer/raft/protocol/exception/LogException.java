package com.daydreamer.raft.protocol.exception;

/**
 * @author Daydreamer
 */
public class LogException extends Exception {
    
    public LogException() {
    }
    
    public LogException(String message) {
        super(message);
    }
    
    public LogException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public LogException(Throwable cause) {
        super(cause);
    }
    
    public LogException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
