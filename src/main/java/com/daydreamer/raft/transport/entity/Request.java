package com.daydreamer.raft.transport.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Daydreamer
 *
 * payload of request
 */
public abstract class Request {
    
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
    private final Map<String, String> headers = new HashMap<String, String>();
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public long getRequestId() {
        return requestId;
    }
    
}
