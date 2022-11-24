package com.daydreamer.raft.api.entity.base;

import com.daydreamer.raft.api.entity.constant.MemberChange;

/**
 * @author Daydreamer
 */
public class MemberChangeEntry {
    
    private String address;
    
    private MemberChange memberChange;
    
    public MemberChangeEntry(String address, MemberChange memberChange) {
        this.address = address;
        this.memberChange = memberChange;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public MemberChange getMemberChange() {
        return memberChange;
    }
    
    public void setMemberChange(MemberChange memberChange) {
        this.memberChange = memberChange;
    }
}
