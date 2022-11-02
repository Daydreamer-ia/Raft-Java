package com.daydreamer.raft.transport.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Daydreamer
 *
 * payload of request
 */
public abstract class Request implements Serializable {
    
    private static final long serialVersionUID = 11988178431L;
    
    /**
     * next request id
     */
    private static final AtomicLong NEXT_REQUEST_ID = new AtomicLong(0);
    
    /**
     * request id
     */
    private long requestId = NEXT_REQUEST_ID.getAndIncrement();
    
    /**
     * data header
     */
    private Map<String, String> headers = new HashMap<String, String>();
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public long getRequestId() {
        return requestId;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
}
