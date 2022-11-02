package com.daydreamer.raft.transport.constant;

/**
 * @author Daydreamer
 *
 * reponse code of response
 */
public class ResponseCode {
    
    private ResponseCode() {}
    
    /**
     * success
     */
    public static final int SUCCESS_CODE = 200;
    
    /**
     * error from client
     */
    public static final int ERROR_CLIENT = 400;
    
    /**
     * error from server
     */
    public static final int ERROR_SERVER = 500;
}
