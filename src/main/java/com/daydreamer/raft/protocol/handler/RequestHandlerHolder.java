package com.daydreamer.raft.protocol.handler;


import com.daydreamer.raft.transport.entity.Request;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Daydreamer
 *
 * request handler registry
 */
public class RequestHandlerHolder {
    
    /**
     * registry
     */
    public static final Map<Class<? extends Request>, RequestHandler<?, ?>> REGISTRY = new ConcurrentHashMap<>();
    
    private static final String HANDLER_PACKAGE = "com.daydreamer.raft.protocol.handler.impl";
    
    /**
     * private constructor
     */
    private RequestHandlerHolder() {}
    
    static {
    
    }
}
