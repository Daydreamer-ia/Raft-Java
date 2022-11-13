package com.daydreamer.raft.api.entity.response;

import com.daydreamer.raft.api.entity.Response;

/**
 * @author Daydreamer
 */
public class PrevoteResponse extends Response {
    
    private boolean agree;
    
    public PrevoteResponse() {
    }
    
    public PrevoteResponse(boolean agree) {
        this.agree = agree;
    }
    
    public boolean isAgree() {
        return agree;
    }
    
    public void setAgree(boolean agree) {
        this.agree = agree;
    }
}
