package com.daydreamer.raft.api.exception;

import com.daydreamer.raft.api.entity.base.ErrorResponse;

/**
 * @author Daydreamer
 */
public class InvalidResponseException extends Exception {
    
    private ErrorResponse errorResponse;
    
    public InvalidResponseException(ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }
    
    public InvalidResponseException(String message, Throwable cause, ErrorResponse errorResponse) {
        super(message, cause);
        this.errorResponse = errorResponse;
    }
    
    public InvalidResponseException(String message, ErrorResponse errorResponse) {
        super(message);
        this.errorResponse = errorResponse;
    }
    
    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
    
    public void setErrorResponse(ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }
}
