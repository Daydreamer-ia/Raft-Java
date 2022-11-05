package com.daydreamer.raft.transport.connection.impl.grpc;

import com.daydreamer.raft.api.entity.Request;
import com.daydreamer.raft.api.entity.Response;
import com.daydreamer.raft.api.entity.base.ErrorResponse;
import com.daydreamer.raft.api.exception.InvalidResponseException;
import com.daydreamer.raft.api.grpc.Message;
import com.daydreamer.raft.api.grpc.RequesterGrpc;
import com.daydreamer.raft.common.entity.SimpleFuture;
import com.daydreamer.raft.common.utils.MsgUtils;
import com.daydreamer.raft.transport.connection.Connection;
import com.daydreamer.raft.transport.connection.ResponseCallBack;
import io.grpc.ManagedChannel;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Daydreamer
 * <p>
 * client to request
 */
public class GrpcConnection extends Connection {
    
    private RequesterGrpc.RequesterBlockingStub requesterBlockingStub;
    
    public GrpcConnection(String id, RequesterGrpc.RequesterBlockingStub requesterFutureStub) {
        super(id);
        this.requesterBlockingStub = requesterFutureStub;
    }
    
    @Override
    public Response request(Request request, long timeout) throws Exception {
        Message msg = MsgUtils.convertMsg(request);
        SimpleFuture<Response> responseFuture = new SimpleFuture<>(() -> {
            Message responseMsg = requesterBlockingStub.request(msg);
            return MsgUtils.convertResponse(responseMsg);
        });
        try {
            Response response = responseFuture.get(timeout, TimeUnit.MICROSECONDS);
            if (response != null) {
                return response;
            } else {
                throw new TimeoutException();
            }
        } catch (InterruptedException e) {
            // nothing to do
        } catch (Exception e) {
            throw responseFuture.getException();
        }
        return null;
    }
    
    @Override
    public Future<Response> request(Request request) {
        Message msg = MsgUtils.convertMsg(request);
        return new SimpleFuture<>(() -> {
            Message responseMsg = requesterBlockingStub.request(msg);
            return MsgUtils.convertResponse(responseMsg);
        });
    }
    
    @Override
    public void request(Request request, long timeout, ResponseCallBack callBack) {
        new SimpleFuture<>(() -> {
            Response result = null;
            try {
                Future<Response> future = request(request);
                Response response = future.get(timeout, TimeUnit.MICROSECONDS);
                if (response instanceof ErrorResponse) {
                    callBack.onFail(new InvalidResponseException((ErrorResponse) response));
                }
                callBack.onSuccess(response);
            } catch (TimeoutException te) {
                System.out.println("超时");
                callBack.onTimeout();
            } catch (Exception e) {
                System.out.println("失败");
                callBack.onFail(e);
            }
            return result;
        });
    }
    
    @Override
    public void request(Request request, ResponseCallBack callBack) throws Exception {
        new SimpleFuture<>(() -> {
            Response result = null;
            try {
                Message responseMsg = requesterBlockingStub.request(MsgUtils.convertMsg(request));
                Response response = MsgUtils.convertResponse(responseMsg);
                if (response instanceof ErrorResponse) {
                    callBack.onFail(new InvalidResponseException((ErrorResponse) response));
                }
                callBack.onSuccess(response);
            } catch (Exception e) {
                callBack.onFail(e);
            }
            return result;
        });
    }
    
    @Override
    public void close() {
        try {
            ((ManagedChannel) requesterBlockingStub.getChannel()).awaitTermination(100, TimeUnit.MICROSECONDS);
        } catch (InterruptedException e) {
            // nothing to do
            e.printStackTrace();
        }
    }
}
