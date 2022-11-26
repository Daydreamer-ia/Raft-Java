package com.daydreamer.raft.protocol.chain.impl;

import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.api.entity.base.Payload;
import com.daydreamer.raft.api.entity.constant.LogType;
import com.daydreamer.raft.api.entity.constant.MemberChange;
import com.daydreamer.raft.protocol.aware.RaftMemberManagerAware;
import com.daydreamer.raft.protocol.chain.LogPostProcessor;
import com.daydreamer.raft.protocol.core.RaftMemberManager;

/**
 * @author Daydreamer
 * <p>
 * handle if member change log append or commit
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
public class MemberChangeLogPostProcessor implements LogPostProcessor, RaftMemberManagerAware {
    
    private volatile long lastMemberChangeLogIndex = -1;
    
    private RaftMemberManager raftMemberManager;
    
    private String MEMBER_CHANGE_TYPE = "memberChange";
    
    private String ADDRESS = "address";
    
    @Override
    public synchronized boolean handleBeforeAppend(LogEntry logEntry) {
        // if not member change log, pass
        if (!LogType.MEMBER_CHANGE.equals(logEntry.getPayload().getLogType())) {
            return true;
        }
        // if member change log, try to check whether cover
        if (lastMemberChangeLogIndex == -1) {
            return true;
        }
        // cover
        return lastMemberChangeLogIndex <= logEntry.getLogId();
    }
    
    @Override
    public synchronized void handleAfterAppend(LogEntry logEntry) {
        // if not member change log, pass
        if (!LogType.MEMBER_CHANGE.equals(logEntry.getPayload().getLogType())) {
            return;
        }
        // update log index
        lastMemberChangeLogIndex = logEntry.getLogId();
    }
    
    @Override
    public synchronized void handleAfterCommit(LogEntry logEntry)  {
        // if not member change log, pass
        if (!LogType.MEMBER_CHANGE.equals(logEntry.getPayload().getLogType())) {
            return;
        }
        // change member list
        Payload payload = logEntry.getPayload();
        // type
        MemberChange memberChange = MemberChange.valueOf(payload.getMetadata().get(MEMBER_CHANGE_TYPE));
        String addr = payload.getMetadata().get(ADDRESS);
        if (MemberChange.ADD.equals(memberChange)) {
            raftMemberManager.addNewMember(addr);
        } else if (MemberChange.REMOVE.equals(memberChange)) {
            // check whether down current node
            if (addr.equals(raftMemberManager.getSelf().getAddress())) {
                raftMemberManager.removeSelf();
            } else {
                raftMemberManager.removeMember(addr);
            }
        }
        lastMemberChangeLogIndex = -1;
        // TODO update config
        
    }
    
    @Override
    public boolean handleBeforeCommit(LogEntry logEntry) {
        // nothing to do
        return true;
    }
    
    @Override
    public void setRaftMemberManager(RaftMemberManager raftMemberManager) {
        this.raftMemberManager = raftMemberManager;
    }
}
