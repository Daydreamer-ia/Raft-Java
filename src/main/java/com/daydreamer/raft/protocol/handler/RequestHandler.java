package com.daydreamer.raft.protocol.handler;

import com.daydreamer.raft.transport.entity.Request;
import com.daydreamer.raft.transport.entity.Response;

/**
 * @author Daydreamer
 *
 * It is a handler to handle request from client
 */
public interface RequestHandler<S extends Request, T extends Response> {
    
    /**
     * handle the request from other node
     *
     * @param request request
     * @return response
     */
    T handle(S request);
    
    /**
     * get data source
     *
     * @return request
     */
    Class<S> getSource();
}
