package com.daydreamer.raft.protocol.core;

import com.daydreamer.raft.api.entity.Request;
import com.daydreamer.raft.api.entity.Response;
import com.daydreamer.raft.api.entity.base.LogEntry;
import com.daydreamer.raft.protocol.entity.Member;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author Daydreamer
 */
public interface LogSender {
    
    /**
     * append log
     *
     * @param member member
     * @param logEntry newest log
     * @throws Exception Exception when appending
     * @return whether success
     */
    boolean appendLog(Member member, LogEntry logEntry)  throws Exception;
    
    /**
     * committed
     *
     * @param request request
     * @param member member
     * @return whether success
     * @throws Exception Exception when committing
     */
    boolean commit(Member member, Request request) throws Exception;
    
    /**
     * send request to all members
     *
     * @param request request
     * @param predicate condition success
     * @param members members
     * @return whether success half of all
     * @throws Exception exception
     */
    boolean batchRequestMembers(Request request, List<Member> members, Predicate<Response> predicate) throws Exception;
    
    
}
