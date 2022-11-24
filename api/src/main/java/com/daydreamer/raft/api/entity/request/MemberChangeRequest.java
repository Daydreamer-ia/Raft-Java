package com.daydreamer.raft.api.entity.request;

import com.daydreamer.raft.api.entity.Request;

import java.util.List;

/**
 * @author Daydreamer
 */
public class MemberChangeRequest extends Request {
    
    private List<String> newMembers;
    
    public MemberChangeRequest(List<String> newMembers) {
        this.newMembers = newMembers;
    }
    
    public List<String> getNewMembers() {
        return newMembers;
    }
    
    public void setNewMembers(List<String> newMembers) {
        this.newMembers = newMembers;
    }
}
