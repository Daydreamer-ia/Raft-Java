package com.daydreamer.raft.transport.connection;

import com.daydreamer.raft.transport.entity.Request;
import com.daydreamer.raft.transport.entity.Response;

import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * @author Daydreamer
 */
public abstract class Connection implements Closeable {
    
    private String id;
    
    public Connection(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * send request
     *
     * @param  request request
     * @param timeout timeout
     * @throws TimeoutException exception if time out
     * @return future
     */
    public abstract Response request(Request request, long timeout) throws Exception;
    
    /**
     * send asyn
     *
     * @param request data
     * @return future
     */
    public abstract Future<Response> request(Request request) throws Exception;
    
    /**
     * call back allow if response
     *
     * @param request request
     * @param timeout timeout
     * @param callBack call back
     * @throws Exception exception
     */
    public abstract void request(Request request, long timeout, ResponseCallBack callBack) throws Exception;
    
    /**
     * call back allow if response
     *
     * @param request request
     * @param callBack call back
     * @throws Exception exception
     */
    public abstract void request(Request request, ResponseCallBack callBack) throws Exception;
}
