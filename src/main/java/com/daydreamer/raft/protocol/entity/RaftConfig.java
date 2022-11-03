package com.daydreamer.raft.protocol.entity;

import java.util.List;

/**
 * @author Daydreamer
 * <p>
 * config about raft
 */
public class RaftConfig {
    
    private List<String> memberIp;
    
    private int heartInterval = 5000;
    
    private int port = 10089;
    
    static {
        // TODO 动态加载配置
        
    }
    
    public List<String> getMemberIp() {
        return memberIp;
    }
    
    public void setMemberIp(List<String> memberIp) {
        this.memberIp = memberIp;
    }
    
    public int getHeartInterval() {
        return heartInterval;
    }
    
    public void setHeartInterval(int heartInterval) {
        this.heartInterval = heartInterval;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
}
