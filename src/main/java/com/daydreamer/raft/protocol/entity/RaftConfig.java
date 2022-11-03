package com.daydreamer.raft.protocol.entity;

import java.util.List;

/**
 * @author Daydreamer
 * <p>
 * config about raft
 */
public class RaftConfig {
    
    private List<String> memberAddresses;
    
    private int heartInterval = 5000;
    
    private int port = 10089;
    
    private int abnormalActiveInterval = 2000;
    
    static {
        // TODO 动态加载配置
        
    }
    
    public int getAbnormalActiveInterval() {
        return abnormalActiveInterval;
    }
    
    public void setAbnormalActiveInterval(int abnormalActiveInterval) {
        this.abnormalActiveInterval = abnormalActiveInterval;
    }
    
    public List<String> getMemberAddresses() {
        return memberAddresses;
    }
    
    public void setMemberAddresses(List<String> memberAddresses) {
        this.memberAddresses = memberAddresses;
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
