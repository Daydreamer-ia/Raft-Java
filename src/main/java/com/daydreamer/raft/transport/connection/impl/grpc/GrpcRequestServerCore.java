package com.daydreamer.raft.transport.connection.impl.grpc;

import com.daydreamer.raft.common.MsgUtils;
import com.daydreamer.raft.protocol.core.ConnectionManager;
import com.daydreamer.raft.protocol.handler.RequestHandlerHolder;
import com.daydreamer.raft.transport.entity.Response;
import com.daydreamer.raft.transport.grpc.Message;
import com.daydreamer.raft.transport.grpc.RequesterGrpc;
import io.grpc.stub.StreamObserver;

/**
 * @author Daydreamer
 *
 * GrpcServer core
 */
public class GrpcRequestServerCore extends RequesterGrpc.RequesterImplBase {
    
    private ConnectionManager connectionManager;
    
    public GrpcRequestServerCore(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    
    @Override
    public void request(Message msg, StreamObserver<Message> responseObserver) {
        // refresh active time
        connectionManager.refreshActiveTime(msg.getId());
        // handle
        Response response = RequestHandlerHolder.handle(MsgUtils.convertRequest(msg));
        // response
        Message back = MsgUtils.convertMsg(response);
        responseObserver.onNext(back);
        responseObserver.onCompleted();
    }
}
