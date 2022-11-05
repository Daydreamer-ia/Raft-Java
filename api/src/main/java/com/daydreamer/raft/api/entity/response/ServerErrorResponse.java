package com.daydreamer.raft.api.entity.response;

import com.daydreamer.raft.api.entity.Response;

/**
 * @author Daydreamer
 *
 * Server error, often unknown error
 */
public class ServerErrorResponse extends Response {
    public ServerErrorResponse(String msg, int code) {
        super.setMessage(msg);
        super.setResultCode(code);
    }
}
