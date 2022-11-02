package com.daydreamer.raft.transport.entity.response;

import com.daydreamer.raft.transport.entity.Response;


/**
 * @author Daydreamer
 *
 * Client error
 */
public class ClientErrorResponse extends Response {
    
    public ClientErrorResponse(String msg, int code) {
        super.setMessage(msg);
        super.setResultCode(code);
    }
}
