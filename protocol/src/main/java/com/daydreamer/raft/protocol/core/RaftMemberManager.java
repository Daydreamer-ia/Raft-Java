package com.daydreamer.raft.protocol.core;

import com.daydreamer.raft.api.entity.Request;
import com.daydreamer.raft.api.entity.Response;
import com.daydreamer.raft.protocol.entity.Member;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author Daydreamer
 * <p>
 * It is used to hold the message of member
 */
public interface RaftMemberManager {
    
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
     * whether current node is leader
     *
     * @return whether current node is leader
     */
    boolean isLeader();
    
    /**
     * send request to all members
     *
     * @param request request
     * @param predicate condition success
     * @return whether success half of all
     * @throws Exception exception
     */
    boolean batchRequestMembers(Request request, Predicate<Response> predicate) throws Exception;
}
