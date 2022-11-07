package com.daydreamer.raft.api.entity.response;

import com.daydreamer.raft.api.entity.Response;

/**
 * @author Daydreamer
 */
public class AppendEntriesResponse extends Response {
    
    /**
     * whether accepted
     */
    private boolean accepted;
    
    /**
     * current follower term
     */
    private int followerTerm;
    
    /**
     * current follower max log id
     */
    private long followerLogId;
    
    public AppendEntriesResponse(boolean accepted, int followerTerm, long followerLogId) {
        this.accepted = accepted;
        this.followerLogId = followerLogId;
        this.followerTerm = followerTerm;
    }
    
    public boolean isAccepted() {
        return accepted;
    }
    
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
    
    public int getFollowerTerm() {
        return followerTerm;
    }
    
    public void setFollowerTerm(int followerTerm) {
        this.followerTerm = followerTerm;
    }
    
    public long getFollowerLogId() {
        return followerLogId;
    }
    
    public void setFollowerLogId(long followerLogId) {
        this.followerLogId = followerLogId;
    }
}
