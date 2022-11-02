package com.daydreamer.raft.transport.entity;

import com.daydreamer.raft.transport.constant.ResponseCode;

import java.io.Serializable;

/**
 * @author Daydreamer
 *
 * payload of response
 */
public abstract class Response implements Serializable {
    
    private int resultCode = ResponseCode.SUCCESS_CODE;
    
    private String message;
    
    private String requestId;
    
    public int getResultCode() {
        return resultCode;
    }
    
    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
