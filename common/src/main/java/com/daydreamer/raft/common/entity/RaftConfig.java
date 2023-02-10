package com.daydreamer.raft.common.entity;

import com.daydreamer.raft.common.annotation.SPI;
import com.daydreamer.raft.common.service.ActiveProperties;

import java.util.List;

/**
 * @author Daydreamer
 * <p>
 * config about raft
 * <p>
 * voteBaseTime + ramdom() < heartInterval < abnormalActiveInterval
 */
@SPI("raftConfig")
public class RaftConfig implements ActiveProperties {
    
    /**
     * member ip exclude current node
     */
    private List<String> memberAddresses;
    
    /**
     * server ip
     */
    private String serverAddr;
    
    /**
     * current node will tell follower to keep if current node is leader and timeout
     */
    private int heartInterval = 1000;
    
    /**
     * current node will ask votes if timeout
     */
    private int abnormalActiveInterval = 10000;
    
    /**
     * base interval between two elections
     */
    private int voteBaseTime = 3000;
    
    /**
     * base wait time in candidate
     */
    private int candidateStatusTimeout = 5000;
    
    /**
     * write fail, then retry <code>writeRetryTimes</code>
     */
    private int writeRetryTimes = 2;

    /**
     * reject any write request if current node is follower
     */
    private boolean followerRejectWrite = false;

    /**
     * the count of core thread for default thread pool
     * {@link com.daydreamer.raft.common.threadpool.impl.CacheThreadPoolFactory}
     */
    private int defaultThreadPoolCoreThread = 2;

    /**
     * the count of max thread for default thread pool
     * {@link com.daydreamer.raft.common.threadpool.impl.CacheThreadPoolFactory}
     */
    private int defaultThreadPoolMaxThread = 2;

    /**
     * log data dir
     */
    private String dataDir;

    public boolean isFollowerRejectWrite() {
        return followerRejectWrite;
    }

    public void setFollowerRejectWrite(boolean followerRejectWrite) {
        this.followerRejectWrite = followerRejectWrite;
    }

    public int getDefaultThreadPoolCoreThread() {
        return defaultThreadPoolCoreThread;
    }

    public void setDefaultThreadPoolCoreThread(int defaultThreadPoolCoreThread) {
        this.defaultThreadPoolCoreThread = defaultThreadPoolCoreThread;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public int getDefaultThreadPoolMaxThread() {
        return defaultThreadPoolMaxThread;
    }

    public void setDefaultThreadPoolMaxThread(int defaultThreadPoolMaxThread) {
        this.defaultThreadPoolMaxThread = defaultThreadPoolMaxThread;
    }

    public String getServerAddr() {
        return serverAddr;
    }
    
    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }
    
    public int getWriteRetryTimes() {
        return writeRetryTimes;
    }
    
    public void setWriteRetryTimes(int writeRetryTimes) {
        this.writeRetryTimes = writeRetryTimes;
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
}
