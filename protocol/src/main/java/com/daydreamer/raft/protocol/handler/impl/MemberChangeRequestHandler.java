package com.daydreamer.raft.protocol.handler.impl;

import com.daydreamer.raft.api.entity.request.MemberChangeRequest;
import com.daydreamer.raft.api.entity.response.MemberChangeResponse;
import com.daydreamer.raft.common.annotation.SPIImplement;
import com.daydreamer.raft.protocol.handler.RequestHandler;
import com.daydreamer.raft.protocol.storage.ReplicatedStateMachine;

/**
 * @author Daydreamer
 * <p>
 * Leader has the activity to change memeber.
 * First, leader will try to append memeber change log(may be add a new memeber or
 * remove a member) to other node.
 * Then, leader will try to apply new member list after majority append successfully.
 * Last, leader will try to commit member change log, and tell follower to do as self.
 * <p>
 * If leader down before finish appending member change log, this member change request will be abandon.
 * If leader down after finish appending member change log. then follower will do continue if it becomes
 * leader because of no-op log.
 * <p>
 * especially, member will enter member change state if it append a log for member changing,
 * it will not accept any member change request unless cover the old member change log
 */
@SPIImplement("memberChangeRequestHandler")
public class MemberChangeRequestHandler implements RequestHandler<MemberChangeRequest, MemberChangeResponse> {
    
    private long lastMemberChangeLogIndex = -1;
    
    private ReplicatedStateMachine replicatedStateMachine;
    
    @Override
    public MemberChangeResponse handle(MemberChangeRequest request) {
        return new MemberChangeResponse();
    }
    
    @Override
    public Class<MemberChangeRequest> getSource() {
        return MemberChangeRequest.class;
    }

    public void setReplicatedStateMachine(ReplicatedStateMachine replicatedStateMachine) {
        this.replicatedStateMachine = replicatedStateMachine;
    }
}
