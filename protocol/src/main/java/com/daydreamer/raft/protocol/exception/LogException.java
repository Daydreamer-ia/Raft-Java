package com.daydreamer.raft.protocol.exception;

/**
 * @author Daydreamer
 */
public class LogException extends Exception {
    
    /**
     * {@link com.daydreamer.raft.protocol.constant.LogErrorCode}
     */
    public int errorCode;
    
    public LogException(int errorCode) {
        this.errorCode = errorCode;
    }
    
    public LogException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public LogException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public LogException(Throwable cause, int errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }
    
    public LogException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, int errorCode) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorCode = errorCode;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
