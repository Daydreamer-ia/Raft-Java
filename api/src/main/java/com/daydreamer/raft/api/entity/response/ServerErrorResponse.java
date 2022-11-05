package com.daydreamer.raft.api.entity.response;

import com.daydreamer.raft.api.entity.base.ErrorResponse;

/**
 * @author Daydreamer
 *
 * Server error, often unknown error
 */
public class ServerErrorResponse extends ErrorResponse {
    public ServerErrorResponse(String msg, int code) {
        super.setMessage(msg);
        super.setResultCode(code);
    }
}
