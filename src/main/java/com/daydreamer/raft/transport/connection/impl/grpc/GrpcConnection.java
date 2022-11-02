package com.daydreamer.raft.transport.connection.impl.grpc;

import com.daydreamer.raft.common.MsgUtils;
import com.daydreamer.raft.transport.connection.Connection;
import com.daydreamer.raft.transport.entity.Request;
import com.daydreamer.raft.transport.entity.Response;
import com.daydreamer.raft.transport.grpc.Message;
import com.daydreamer.raft.transport.grpc.RequesterGrpc;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Daydreamer
 * <p>
 * client to request
 */
public class GrpcConnection extends Connection implements AutoCloseable {
    
    private RequesterGrpc.RequesterFutureStub requesterFutureStub;
    
    public GrpcConnection(String id, RequesterGrpc.RequesterFutureStub requesterFutureStub) {
        super(id);
        this.requesterFutureStub = requesterFutureStub;
    }
    
    @Override
    public Response request(Request request, long timeout) throws TimeoutException {
        Message msg = MsgUtils.convertMsg(request);
        ListenableFuture<Message> future = requesterFutureStub.request(msg);
        Message responseMsg = null;
        try {
            responseMsg = future.get(timeout, TimeUnit.MICROSECONDS);
        } catch (Exception e) {
            throw new TimeoutException();
        }
        return MsgUtils.convertResponse(responseMsg);
    }
    
    @Override
    public Future<Response> request(Request request) {
        Message msg = MsgUtils.convertMsg(request);
        ListenableFuture<Message> future = requesterFutureStub.request(msg);
        return new Future<Response>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public boolean isCancelled() {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public boolean isDone() {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public Response get() throws InterruptedException, ExecutionException {
                return MsgUtils.convertResponse(future.get());
            }
            
            @Override
            public Response get(long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
                return MsgUtils.convertResponse(future.get(timeout, unit));
            }
        };
    }
    
    @Override
    public void close() throws Exception {
        ((ManagedChannel) requesterFutureStub.getChannel()).awaitTermination(100, TimeUnit.MICROSECONDS);
    }
}
