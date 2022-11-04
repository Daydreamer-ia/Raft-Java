package com.daydreamer.raft.transport.connection;

import com.daydreamer.raft.transport.entity.Response;

/**
 * @author Daydreamer
 */
public interface ResponseCallBack {
    
    /**
     * do for success
     *
     * @param response response
     */
    void onSuccess(Response response);
    
    /**
     * do for failure
     *
     * @param e exception
     */
    void onFail(Exception e);
    
    /**
     * do for timeout
     */
    void onTimeout();
}
