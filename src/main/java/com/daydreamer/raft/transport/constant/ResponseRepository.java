package com.daydreamer.raft.transport.constant;


import com.daydreamer.raft.transport.entity.Response;
import com.daydreamer.raft.transport.entity.response.ClientErrorResponse;
import com.daydreamer.raft.transport.entity.response.ServerErrorResponse;

/**
 * @author Daydreamer
 *
 * Easy to get static response
 */
public class ResponseRepository {
    
    public static final Response NOT_HANDLER_FOUND = new ClientErrorResponse("Not handler found", ResponseCode.ERROR_CLIENT);
    
    public static final Response SERVER_UNKNOWN_ERROR = new ServerErrorResponse("Unknown error from server", ResponseCode.ERROR_SERVER);
    
}
