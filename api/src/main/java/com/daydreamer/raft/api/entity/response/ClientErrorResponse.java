package com.daydreamer.raft.api.entity.response;

import com.daydreamer.raft.api.entity.Response;


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
