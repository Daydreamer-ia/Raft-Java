package com.daydreamer.raft.protocol.core;

import com.daydreamer.raft.protocol.entity.Member;
import com.daydreamer.raft.protocol.exception.LogException;
import com.daydreamer.raft.transport.connection.Closeable;

import java.util.List;

/**
 * @author Daydreamer
 * <p>
 * It is used to hold the message of member
 */
public interface RaftMemberManager extends Closeable {
    
    /**
     * init manager
     */
    void init();
    
    /**
     * get all members
     *
     * @return all members
     */
    List<Member> getAllMember();
    
    /**
     * get all normal members it is valid if current node is leader
     *
     * @return normal members
     */
    List<Member> getActiveMember();
    
    /**
     * add a new member may success if current node is leader, fail if follower
     *
     * @param addr new member
     * @return whether add successfully
     * @throws LogException log exception
     */
    boolean addNewMember(String addr) throws LogException;
    
    /**
     * remove a existed member
     *
     * @param id member addr
     * @return whether remove successfully
     */
    boolean removeMember(String id);
    
    /**
     * whether changing member
     *
     * @return whether changing member
     */
    boolean isMemberChanging();
    
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
     * whether current node is leader
     *
     * @return whether current node is leader
     */
    boolean isLeader();
}
