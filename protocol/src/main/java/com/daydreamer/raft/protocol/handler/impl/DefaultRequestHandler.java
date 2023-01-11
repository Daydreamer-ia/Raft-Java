package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.api.entity.Request;
import com.daydreamer.raft.api.entity.Response;
import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.transport.constant.ResponseRepository;

/**
 * @author Daydreamer
 */
@SPIImplement("defaultRequestHandler")
public class DefaultRequestHandler implements RequestHandler<Request, Response> {

    @Override
    public Response handle(Request request) {
        return ResponseRepository.SERVER_UNKNOWN_ERROR;
    }

    @Override
    public Class<Request> getSource() {
        return Request.class;
    }
}
