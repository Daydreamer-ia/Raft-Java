package com.daydreamer.raft.api.entity.response;

import com.daydreamer.raft.api.entity.base.ErrorResponse;


/**
 * @author Daydreamer
 *
 * Client error
 */
public class ClientErrorResponse extends ErrorResponse {
    
    public ClientErrorResponse(String msg, int code) {
        super.setMessage(msg);
        super.setResultCode(code);
    }
}
