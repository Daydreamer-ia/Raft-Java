package com.daydreamer.raft.protocol.entity;

import java.util.List;

/**
 * @author Daydreamer
 * <p>
 * config about raft
 *
 * voteBaseTime + ramdom() < heartInterval < abnormalActiveInterval
 */
public class RaftConfig {
    
    /**
     * member ip exclude current node
     */
    private List<String> memberAddresses;
    
    /**
     * current node will tell follower to keep if current node is leader and timeout
     */
    private int heartInterval = 1000;
    
    /**
     * server port
     */
    private int port = 10089;
    
    /**
     * current node will ask votes if timeout
     */
    private int abnormalActiveInterval = 6000;
    
    /**
     * base interval between two elections
     */
    private int voteBaseTime = 5000;
    
    /**
     * base wait time in candidate
     */
    private int candidateStatusTimeout = 5000;
    
    static {
        // TODO 动态加载配置
        
    }
    
    public int getCandidateStatusTimeout() {
        return candidateStatusTimeout;
    }
    
    public void setCandidateStatusTimeout(int candidateStatusTimeout) {
        this.candidateStatusTimeout = candidateStatusTimeout;
    }
    
    public int getVoteBaseTime() {
        return voteBaseTime;
    }
    
    public void setVoteBaseTime(int voteBaseTime) {
        this.voteBaseTime = voteBaseTime;
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
