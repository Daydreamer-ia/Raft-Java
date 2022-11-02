package com.daydreamer.raft.transport.connection;

import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * @author Daydreamer
 */
public abstract class Connection {
    
    private String ip;
    
    private int port;
    
    private String addr;
    
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getAddr() {
        return addr;
    }
    
    public void setAddr(String addr) {
        this.addr = addr;
    }
    
    /**
     * send asyn
     *
     * @param o data
     * @throws TimeoutException exception if time out
     * @return future
     */
    public abstract Future<Boolean> requestSyn(Object o) throws TimeoutException;
    
    /**
     * send message
     *
     * @throws Exception if exception
     */
    public abstract void request() throws Exception;
}
