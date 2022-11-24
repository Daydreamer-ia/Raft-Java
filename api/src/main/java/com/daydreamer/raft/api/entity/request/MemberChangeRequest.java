package com.daydreamer.raft.api.entity.request;

import com.daydreamer.raft.api.entity.Request;
import com.daydreamer.raft.api.entity.base.MemberChangeEntry;

/**
 * @author Daydreamer
 */
public class MemberChangeRequest extends Request {
    
    private MemberChangeEntry memberChangeEntry;
    
    public MemberChangeRequest(MemberChangeEntry memberChangeEntry) {
        this.memberChangeEntry = memberChangeEntry;
    }
    
    public MemberChangeEntry getMemberChangeEntry() {
        return memberChangeEntry;
    }
    
    public void setMemberChangeEntry(MemberChangeEntry memberChangeEntry) {
        this.memberChangeEntry = memberChangeEntry;
    }
}
