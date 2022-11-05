package com.daydreamer.raft.transport.connection.impl.http;

import com.daydreamer.raft.api.entity.Request;
import com.daydreamer.raft.api.entity.Response;
import com.daydreamer.raft.transport.connection.Connection;
import com.daydreamer.raft.transport.connection.ResponseCallBack;

import java.util.concurrent.Future;

/**
 * @author Daydreamer
 *
 * TODO No plan at the moment
 */
public class HttpConnection extends Connection {
    
    public HttpConnection(String id) {
        super(id);
        throw new UnsupportedOperationException("No plain current version");
    }
    
    @Override
    public Response request(Request request, long timeout) throws Exception {
        return null;
    }
    
    @Override
    public Future<Response> request(Request request) throws Exception {
        return null;
    }
    
    @Override
    public void request(Request request, long timeout, ResponseCallBack callBack) throws Exception {
    
    }
    
    @Override
    public void request(Request request, ResponseCallBack callBack) throws Exception {
    
    }
    
    @Override
    public void close() {
    
    }
}
