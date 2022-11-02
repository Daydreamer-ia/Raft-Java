package com.daydreamer.raft.protocol.core;

import com.daydreamer.raft.protocol.entity.Member;

import java.util.List;

/**
 * @author Daydreamer
 * <p>
 * It is used to hold the message of member
 */
public interface RaftMemberManager {
    
    /**
     * get all members
     *
     * @return all members
     */
    List<Member> getAllMember();
    
    /**
     * get all normal members
     *
     * @return normal members
     */
    List<Member> getActiveMember();
    
    /**
     * whether current node is leader
     *
     * @return whether current node is leader
     */
    boolean isLeader();
    
    /**
     * add a new member
     * may success if current node is leader, fail if follower
     *
     * @param member new member
     * @return whether add successfully
     */
    boolean addNewMember(Member member);
    
    /**
     * get self as member
     *
     * @return self as member
     */
    Member getSelf();
    
    /**
     * get member by id
     *
     * @param id id of member
     * @return member
     */
    Member getMemberById(String id);
    
    /**
     * clean request id
     *
     * @param id member id
     * @return whether success
     */
    boolean restartMember(String id);
    
    /**
     * refresh active time
     *
     * @param id member id
     */
    void refreshMemberActive(String id);
}
