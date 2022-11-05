package com.daydreamer.raft.protocol.core.impl;

import com.daydreamer.raft.api.grpc.Message;
import com.daydreamer.raft.api.grpc.RequesterGrpc;
import com.daydreamer.raft.common.utils.MsgUtils;
import com.daydreamer.raft.api.entity.Response;
import com.daydreamer.raft.protocol.handler.RequestHandlerHolder;
import io.grpc.stub.StreamObserver;

/**
 * @author Daydreamer
 *
 * GrpcServer core
 */
public class GrpcRequestServerCore extends RequesterGrpc.RequesterImplBase {
    
    @Override
    public void request(Message msg, StreamObserver<Message> responseObserver) {
        // handle
        Response response = RequestHandlerHolder.handle(MsgUtils.convertRequest(msg));
        // response
        Message back = MsgUtils.convertMsg(response);
        responseObserver.onNext(back);
        responseObserver.onCompleted();
    }
}
