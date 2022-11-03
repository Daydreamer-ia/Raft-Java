package com.daydreamer.raft.transport.entity.response;

import com.daydreamer.raft.transport.entity.Response;

/**
 * @author Daydreamer
 */
public class SetupResponse extends Response {
    
    private String memberId;
    
    public SetupResponse(String memberId) {
        this.memberId = memberId;
    }
    
    public String getMemberId() {
        return memberId;
    }
    
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }
}
